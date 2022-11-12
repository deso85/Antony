package bot.antony.commands.giveaway;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bot.antony.Antony;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * The State Machine gathers all information to start a {@link bot.antony.commands.giveaway.Giveaway Giveaway}
 * via an interactive dialogue.
 *
 * @since  7.6.0
 * @author deso85
 */
public class GiveawayStateMachine extends ListenerAdapter {
	private MessageChannel channel;
	private int state = 1; // 1: Description; 2: Countdown; 3: Channel; 4: Winner Count
	private String reactionMsgID;
	private User gaSponsor;
	private String gaDescription;
	private int gaRuntimeInMin;
	private TextChannel gaChannel;
	private int gaWinCount;

	/**
     * Constructs a new GiveawayStateMachine instance, which is used to gather all information to start a
     * {@link bot.antony.commands.giveaway.Giveaway Giveaway} via an interactive dialogue.
     */
	public GiveawayStateMachine(MessageChannel channel, User author) {
		this.channel = channel;
		this.gaSponsor = author;
	}

	/**
	 * Part of the State Machine which checks the user response in the chat.
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return; // don't respond to other bots
		if (event.getChannel() != channel)
			return; // ignore other channels
		if (event.getAuthor() != gaSponsor)
			return; // only accept input from sponsor
		if (event.getMessage().getContentRaw().startsWith("!") || event.getMessage().getContentRaw().toLowerCase().contains("stop")) {
			event.getJDA().removeEventListener(this);
			return;
		}
		
		switch(state) {
			//Description for giveaway set
			case 1:
				postDescriptionVerification(event);
				break;
			//Duration for giveaway set
			case 2:
				postDurationVerification(event);
				break;
			//Channel for giveaway set
			case 3:
				postChannelVerification(event);
				break;
			//Set winner count
			case 4:
				postWinnerCountVerification(event);
				break;
			//End State Machine
			default:
				event.getJDA().removeEventListener(this);
				break;
		}
	}

	/**
	 * Part of the State Machine which checks the users reactions on questions.
	 */
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (event.getMember().getUser().isBot())
			return; // don't respond to bots
		if (event.getMember().getUser() != gaSponsor)
			return; // only accept input from sponsor
		if (event.getMessageId().equals(reactionMsgID)) {
			
			if (state == 1) { //Description approval
				
				if (event.getEmoji().getFormatted().equals("✅")) {
					channel.sendMessage("Wie lange soll das Giveaway laufen?"
							+ "\n*(Beispiele: `5d` = 5 Tage; `4h` = 4 Std.; `3m` = 3 Min.; `3h 5m` = 3 Std. und 5 Min.)*").queue();
					state = 2;
				} else if (event.getEmoji().getFormatted().equals("❌")) {
					channel.sendMessage("Was willst du verschenken und welche Bedingungen gibt es? *(Freitext)"
							+ "\n`stop` beendet den Dialog*").queue();
				}
				
			} else if (state == 2) { //Runtime approval
				
				if (event.getEmoji().getFormatted().equals("✅")) {
					channel.sendMessage("In welchem Kanal soll das Giveaway gestartet werden?").queue();
					state = 3;
				} else if (event.getEmoji().getFormatted().equals("❌")) {
					channel.sendMessage("Wie lange soll das Giveaway laufen?"
							+ "\n*(Beispiele: `5d` = 5 Tage; `4h` = 4 Std.; `3m` = 3 Min.; `3h 5m` = 3 Std. und 5 Min.)*").queue();
				}
				
			} else if (state == 3) { //Channel approval
				
				if (event.getEmoji().getFormatted().equals("✅")) {
					channel.sendMessage("Wie viele Gewinner darf es geben?").queue();
					state = 4;
				} else if (event.getEmoji().getFormatted().equals("❌")) {
					channel.sendMessage("In welchem Kanal soll das Giveaway gestartet werden?").queue();
				}
				
			} else if (state == 4) { //Final approval
				
				if (event.getEmoji().getFormatted().equals("✅")) {
					Antony.getGiveawayController().addGA(gaSponsor, gaDescription, gaChannel, gaRuntimeInMin, gaWinCount);
				} else if (event.getEmoji().getFormatted().equals("❌")) {
					channel.sendMessage("Giveaway abgebrochen ...").queue();
				}
				event.getJDA().removeEventListener(this);
				
			}
		}
	}
	
	/**
	 * Function to parse the user input for {@link bot.antony.commands.giveaway.Giveaway Giveaway} duration.
	 * 
	 * @param  usrInput
	 *         String to parse.
	 * @return int
	 *         duration in minutes.
	 */
	private int parseDuration(String usrInput) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("[GiveawayStateMachine] Analyze user input for duration: " + usrInput + " -> ");
		
		int pim = 0;		
		Pattern pattern = Pattern.compile("\\d+[dhm]");
		Matcher matcher = pattern.matcher(usrInput);
		while(matcher.find()) {
			String unit = matcher.group();
			char timeType = unit.charAt(unit.length() - 1);
			int timeAmount = Integer.parseInt(unit.substring(0, unit.length() - 1));
			if(timeType == 'd') {
				pim += TimeUnit.DAYS.toMinutes(timeAmount);
				logMsg.append(TimeUnit.DAYS.toDays(timeAmount) + " DAYS ");
			}
	        
			if(timeType == 'h') {
				pim += TimeUnit.HOURS.toMinutes(timeAmount);
				logMsg.append(TimeUnit.HOURS.toHours(timeAmount) + " HOURS ");
			}
	        
			if(timeType == 'm') {
				pim += TimeUnit.MINUTES.toMinutes(timeAmount);
				logMsg.append(TimeUnit.MINUTES.toMinutes(timeAmount) + " MINUTES ");
			}
		}
		logMsg.append(" --> PIM: " + pim);
		Antony.getLogger().debug(logMsg.toString());
		return pim;
	}
	
	/**
	 * Verify if {@link bot.antony.commands.giveaway.Giveaway Giveaway} description is correct.
	 * 
	 * @param  event
	 *         MessageReceivedEvent with answer from the user.
	 */
	private void postDescriptionVerification(MessageReceivedEvent event) {
		gaDescription = event.getMessage().getContentDisplay();
		event.getMessage().reply("Ist die Beschreibung vollständig?").queue(msg -> {
			msg.addReaction(Emoji.fromUnicode("✅")).queue();
			msg.addReaction(Emoji.fromUnicode("❌")).queue();
			reactionMsgID = msg.getId();
		});
	}
	
	/**
	 * Verify if {@link bot.antony.commands.giveaway.Giveaway Giveaway} duration is correct.
	 * 
	 * @param  event
	 *         MessageReceivedEvent with answer from the user.
	 */
	private void postDurationVerification(MessageReceivedEvent event) {
		gaRuntimeInMin = parseDuration(event.getMessage().getContentStripped());
		if (gaRuntimeInMin == 0) {
			channel.sendMessage("Ich konnte die Dauer des Giveaways nicht ermitteln. Versuche es bitte noch einmal."
					+ "\n*(Beispiele: `5d` = 5 Tage; `4h` = 4 Std.; `3m` = 3 Min.; `3h 5m` = 3 Std. und 5 Min.)*").queue();
		} else {
			int pim = gaRuntimeInMin;
			long days = TimeUnit.MINUTES.toDays(pim);
			pim -= days*24*60;
			long hours = TimeUnit.MINUTES.toHours(pim);
			pim -= hours*60;
			long minutes = TimeUnit.MINUTES.toMinutes(pim);
			
			StringBuilder msgResponse = new StringBuilder();
			msgResponse.append("Soll das Giveaway so lange laufen?\n*");
			if(days>0)
				msgResponse.append(days + " Tage, ");
			if(days>0 || hours>0)
				msgResponse.append(hours + " Stunden und ");
			msgResponse.append(minutes + " Minuten*");
			channel.sendMessage(msgResponse.toString()).queue(msg -> {
				msg.addReaction(Emoji.fromUnicode("✅")).queue();
				msg.addReaction(Emoji.fromUnicode("❌")).queue();
				reactionMsgID = msg.getId();
			});
		}
	}
	
	/**
	 * Verify if {@link bot.antony.commands.giveaway.Giveaway Giveaway} channel is correct.
	 * 
	 * @param  event
	 *         MessageReceivedEvent with answer from the user.
	 */
	private void postChannelVerification(MessageReceivedEvent event) {
		if(event.getMessage().getMentions().getChannels().size() > 0
				&& event.getMessage().getMentions().getChannels().get(0).getType() == ChannelType.TEXT) {
			gaChannel = (TextChannel) event.getMessage().getMentions().getChannels().get(0);
			channel.sendMessage("Wie viele Gewinner darf es geben?").queue();
			state = 4;
		} else {
			if(event.getGuild().getTextChannelsByName(event.getMessage().getContentDisplay(), true).size() == 1) {
				gaChannel = event.getGuild().getTextChannelsByName(event.getMessage().getContentDisplay(), true).get(0);
				channel.sendMessage("Ist das der richtige Kanal?"
						+ "\n*" + gaChannel.getAsMention() + "*").queue(msg -> {
					msg.addReaction(Emoji.fromUnicode("✅")).queue();
					msg.addReaction(Emoji.fromUnicode("❌")).queue();
					reactionMsgID = msg.getId();
				});
			} else {
				channel.sendMessage("Bitte verlinke einen passenden Kanal."
						+ "\n*(Beispiel: " + event.getGuild().getDefaultChannel().getAsMention() + ")*").queue();
			}
		}
	}
	
	/**
	 * Verify how many people can win the {@link bot.antony.commands.giveaway.Giveaway Giveaway}.
	 * 
	 * @param  event
	 *         MessageReceivedEvent with answer from the user.
	 */
	private void postWinnerCountVerification(MessageReceivedEvent event) {
		if(Utils.isNumeric(event.getMessage().getContentDisplay())) {
			gaWinCount = Integer.parseInt(event.getMessage().getContentDisplay());
			channel.sendMessage("Alle Angaben sind vollständig. Soll ich das Giveaway verbindlich starten?").queue(msg -> {
				msg.addReaction(Emoji.fromUnicode("✅")).queue();
				msg.addReaction(Emoji.fromUnicode("❌")).queue();
				reactionMsgID = msg.getId();
			});
		} else {
			channel.sendMessage("Wie viele Gewinner darf es geben? *(Als ganze Zahl, z.B.: **2**)*").queue();
		}
	}

}