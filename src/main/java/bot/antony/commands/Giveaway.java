package bot.antony.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class Giveaway implements ServerCommand {

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		final Guild guild = channel.getGuild();
		TextChannel gaChannel = channel;

		// If giveaway will be started in another channel
		if(message.getMentionedChannels().size() > 0) {
			gaChannel = message.getMentionedChannels().get(0);
		}
		
		if(userMessage.length >= 4) {

			switch (userMessage[1].toLowerCase()) {
			case "end": // Ends a giveaway which wasn't initiated by Antony
				
				// Collect emojis
				/*String content = message.getContentRaw();
				List<String> emojis = EmojiParser.extractEmojis(content);
				List<String> customEmoji = message.getEmotes().stream()
				        .map((emote) -> emote.getName() + ":" + emote.getId())
				        .collect(Collectors.toList());

				// Create merged list
				List<String> mergedEmojis = new ArrayList<>();
				mergedEmojis.addAll(emojis);
				mergedEmojis.addAll(customEmoji);

				// Sort based on index in message to preserve order
				mergedEmojis.sort(Comparator.comparingInt(content::indexOf));

				for (String emoji : mergedEmojis) {
					System.out.println(emoji.toString() + " " + emoji.length());
					channel.sendMessage(emoji).queue();
				}*/
				
				String[] giveawayLink = userMessage[2].split("/");
				if(giveawayLink.length >= 3) {
					String gaChannelId = giveawayLink[giveawayLink.length-2];
					String gaMessageId = giveawayLink[giveawayLink.length-1];
					String gaMessageEmote = userMessage[3];
					int gaWinnerCount = 1;
					
					gaChannel = guild.getTextChannelById(gaChannelId);
					Message gaMessage = gaChannel.retrieveMessageById(gaMessageId).complete();
					User gaOrganizer = gaMessage.getAuthor();
					List<User> gaUser = new ArrayList<User>();
					
					if(userMessage.length >= 5) {
						try {
							gaWinnerCount = Integer.parseInt(userMessage[4]);
						}
						catch (NumberFormatException e)
						{
							channel.sendMessage("Du musst die Anzahl Gewinner als ganze Zahl angeben. Ich gehe von einem Gewinner aus.\n\n").queue();
						}
					}
					
					// Only giveaway organizer may end the giveaway
					if(message.getAuthor().equals(gaOrganizer)) {
						
						List<MessageReaction> msgReactions = gaMessage.getReactions();
						for(MessageReaction reaction: msgReactions) {
							String reactionEmoji = reaction.getReactionEmote().getName();
							
							if(reactionEmoji.equals(gaMessageEmote)) {
								//System.out.println(gaMessageEmote + " yep, gleich");
								gaUser = reaction.retrieveUsers().complete();
							}
						}
						
						if(gaUser.size() > 0) {
							Random rand = new Random();
							StringBuilder msg = new StringBuilder();
							msg.append("Folgende Benutzer haben am Giveaway von " + gaMessage.getAuthor().getAsMention() + " teilgenommen:\n");
							int counter = 1;
							for(User usr: gaUser) {
								msg.append("`" + usr.getName() + "`");
								if(counter < gaUser.size()) {
									msg.append(", ");
									counter++;
								}
							}
							msg.append("\n\nEs können bis zu `" + gaWinnerCount + "` Gewinner ausgelost werden.");
							
							HashMap<String, User> gaWinner = new HashMap<String, User>();
							while(gaWinner.size() < gaWinnerCount && gaWinner.size() < gaUser.size()) {
								User possibleWinner = gaUser.get(rand.nextInt(gaUser.size()));
								gaWinner.put(possibleWinner.getId(), possibleWinner);
							}
							
							if(gaWinner.size() == 1) {
								msg.append("\n\n:tada: Gewonnen hat: ");
							} else {
								msg.append("\n\n:tada: Gewonnen haben: ");
							}
							
							int userCounter = 1;
							for(HashMap.Entry<String, User> userEntry: gaWinner.entrySet()) {
								User winner = userEntry.getValue();
								msg.append(winner.getAsMention());
								if(userCounter < gaWinner.size()) {
									msg.append(", ");
									userCounter++;
								}
							}
							msg.append(". Herzlichen Glückwunsch! :partying_face:");
							//msg.append("\n\n:tada: Gewonnen hat: " + gaUser.get(rand.nextInt(gaUser.size())).getAsMention() + ". Herzlichen Glückwunsch! :partying_face:");
							channel.sendMessage(msg.toString()).queue();
							
						} else {
							channel.sendMessage("Es hat niemand am Giveaway teilgenommen oder du hast den Befehl falsch verwendet.\n\n" + printHelp()).queue();
						}
					} else {
						channel.sendMessage("Das Giveaway darf nur von demjenigen aufgelöst werden, der es veranstaltet. Netter Versuch :stuck_out_tongue:").queue();
					}
				} else {
					channel.sendMessage(printHelp()).queue();
				}
				
				break;
			default: // Should start a new giveaway
				
				channel.sendMessage(printHelp()).queue();
				
				break;
			}
		} else {
			channel.sendMessage(printHelp()).queue();
		}
		
		//new DateTime();
		//DateTime datetime = new DateTime(DateTime.now());
		//System.out.println(datetime);
		
	}
	

	public String printHelp() {
		StringBuilder helpText = new StringBuilder();
		helpText.append("So nutzt du " + Antony.getCmdPrefix() + "giveaway:\n");

		helpText.append("***"+ Antony.getCmdPrefix() + "giveaway end <Nachrichtenlink> <Emoji> [Anzahl Gewinner]*** - Beendet ein Giveaway, dass nicht über Antony gestartet wurde. Wenn die Anzahl der Gewinner nicht angegeben wird, wird nur eine Person ausgelost.\n");
		helpText.append("***Beispiel:*** *"+ Antony.getCmdPrefix() + "giveaway end https://discord.com/channels/375031723601297409/605451097699647665/798303589763252224 :tada:*\n\n");
		
		//helpText.append("***"+ Antony.getCmdPrefix() + "giveaway <Laufzeit> <Anzahl Gewinner> <Preis + Beschreibung> [Kanal]*** - Startet ein Giveaway.\n");
		//helpText.append("***Beispiel:*** *"+ Antony.getCmdPrefix() + "giveaway 3d 1 Der Gewinner bekommt eine Lasius niger Kolonie aus dem Schwarmflug 2020. #giveaways*\n\n");
		return helpText.toString();
	}

}
