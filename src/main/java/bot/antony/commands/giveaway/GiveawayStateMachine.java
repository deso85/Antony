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

public class GiveawayStateMachine extends ListenerAdapter {
	private MessageChannel channel;
	private int state = 1; // 1: Description; 2: Countdown; 3: Channel; 4: Winner Count
	private String reactionMsgID;
	private User gaSponsor;
	private String gaDescription;
	private int gaRuntimeInMin;
	private TextChannel gaChannel;
	private int gaWinCount;

	public GiveawayStateMachine(MessageChannel channel, User author) {
		this.channel = channel;
		this.gaSponsor = author;
	}

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
		
		if (state == 1) { //Description for giveaway set
			gaDescription = event.getMessage().getContentDisplay();
			event.getMessage().reply("Ist die Beschreibung vollständig?").queue(msg -> {
				msg.addReaction(Emoji.fromUnicode("✅")).queue();
				msg.addReaction(Emoji.fromUnicode("❌")).queue();
				reactionMsgID = msg.getId();
			});
			
		} else if (state == 2) { //runtime for giveaway set
			
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
			
		} else if (state == 3) { //channel for giveaway set
			
			if(event.getMessage().getMentions().getChannels().size() > 0
					&& event.getMessage().getMentions().getChannels().get(0).getType() == ChannelType.TEXT) {
				gaChannel = (TextChannel) event.getMessage().getMentions().getChannels().get(0);
				channel.sendMessage("Wie viele Gewinner darf es geben?").queue();
				state = 4;
			} else {
				if(event.getGuild().getTextChannelsByName(event.getMessage().getContentDisplay(), true).size() == 1
						&& event.getGuild().getTextChannelsByName(event.getMessage().getContentDisplay(), true).get(0).getType() == ChannelType.TEXT) {
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
			
		} else if (state == 4) { //set winner count
			
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
			
		} else {
			event.getJDA().removeEventListener(this);
			return;
		}
	}

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
	
	private int parseDuration(String usrInput) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("Analyze user input for duration: " + usrInput + " -> ");
		
		int pim = 0;		
		Pattern pattern = Pattern.compile("\\d+[dhm]");
		Matcher matcher = pattern.matcher(usrInput);
		while(matcher.find()) {
			String unit = matcher.group();
			char timeType = unit.charAt(unit.length() - 1);
			int timeAmount = Integer.parseInt(unit.substring(0, unit.length() - 1));
			if(timeType == 'd') {
				//System.out.println(TimeUnit.DAYS.toDays(timeAmount) + " DAYS");
				pim += TimeUnit.DAYS.toMinutes(timeAmount);
				logMsg.append(TimeUnit.DAYS.toDays(timeAmount) + " DAYS ");
			}
	        
			if(timeType == 'h') {
				//System.out.println(TimeUnit.HOURS.toSeconds(timeAmount) + " HOURS");
				pim += TimeUnit.HOURS.toMinutes(timeAmount);
				logMsg.append(TimeUnit.HOURS.toHours(timeAmount) + " HOURS ");
			}
	        
			if(timeType == 'm') {
				//System.out.println(TimeUnit.MINUTES.toMinutes(timeAmount) + " MINUTES");
				pim += TimeUnit.MINUTES.toMinutes(timeAmount);
				logMsg.append(TimeUnit.MINUTES.toMinutes(timeAmount) + " MINUTES ");
			}
		}
		logMsg.append(" --> PIM: " + pim);
		Antony.getLogger().info(logMsg.toString());
		return pim;
	}

}