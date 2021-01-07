package bot.antony.commands;

import java.util.ArrayList;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.notifications.ChannelData;
import bot.antony.notifications.GuildData;
import bot.antony.notifications.NotificationController;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class ChannelUpdateNotification implements ServerCommand {

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		NotificationController nc = Antony.getNotificationController();
		String userID = message.getAuthor().getId();
		GuildData gd = new GuildData(message.getGuild().getId(), message.getGuild().getName());
		
		// Exp. Parameter: (on|off|stats|statistics) #MentionedChannel
		if(message.getMentionedChannels().size() > 0) {
			ArrayList<TextChannel> channelsAdddedTo = new ArrayList<TextChannel>();
			ArrayList<TextChannel> channelsRemovedFrom = new ArrayList<TextChannel>();
			ArrayList<TextChannel> channelsUnchanged = new ArrayList<TextChannel>();
			
			
			//for each mentioned channel...
			for(TextChannel tc: message.getMentionedChannels()) {
				ChannelData cd = new ChannelData(tc.getId(), tc.getName());
				Antony.getLogger().debug(cd.toString());
				
				switch (userMessage[1].toLowerCase()) {

				case "on": // If user wants to receive notifications for channel
						if(nc.addNotification(gd, cd, userID)) {
							channelsAdddedTo.add(tc);
						} else {
							channelsUnchanged.add(tc);
						}
					break;
				case "off": // If user doesn't want to receive notifications for channel
						if(nc.removeNotification(gd, cd, userID)) {
							channelsRemovedFrom.add(tc);
						} else {
							channelsUnchanged.add(tc);
						}
					break;
				case "stats":
				case "statistics": // If user want's to see who has notifications turned on for specified channel
						
						ArrayList<String> usrList = nc.getUserIDsforChannel(gd, cd);
						StringBuilder msg = new StringBuilder();
						
						if(!usrList.isEmpty()) {
							msg.append("Folgende Benutzer werden über Aktualisierungen im Kanal " + tc.getAsMention() + " informiert:\n");
							int counter = 1;
							for(String usr: usrList) {
								msg.append(message.getGuild().getMemberById(usr).getEffectiveName());
								if(counter < usrList.size()) {
									msg.append(", ");
								}
								counter++;
							}
							channel.sendMessage(msg.toString()).queue();
						} else {
							msg.append("Es wird aktuell niemand über Aktualisierungen im Kanal " + tc.getAsMention() + " informiert.");
							channel.sendMessage(msg.toString()).queue();
						}
						
					break;
				default: // Toggle - Turns on if user doesn't receive notifications and vice versa
						if(nc.toggleNotification(gd, cd, userID)) {
							channelsAdddedTo.add(tc);
						} else {
							channelsRemovedFrom.add(tc);
						}
					break;
				}
			}
			
			if(!channelsAdddedTo.isEmpty() || !channelsRemovedFrom.isEmpty() || !channelsUnchanged.isEmpty()) {
				//Prepare message for PN
				EmbedBuilder eb = new EmbedBuilder().setTitle("Einstellungen - Benachrichtigungen über Kanal-Updates")
						.setColor(Antony.getBaseColor())
						.setThumbnail(channel.getGuild().getIconUrl())
						.setDescription("Folgende Änderungen wurden an den Einstellungen zur Benachrichtigung bei Kanal-Updates auf dem Server [" + gd.getName() + "](https://discord.com/channels/" + gd.getId() + ") durchgeführt.")
						.setFooter("Antony | Version " + Antony.getVersion());
				
				if(!channelsAdddedTo.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					for(TextChannel tc: channelsAdddedTo) {
						sb.append("- [#" + tc.getName() + "](https://discord.com/channels/" + gd.getId() + "/" + tc.getId() + ")\n");
						
					}
					eb.addField("Benachrichtigungen aktiviert", sb.toString(), false);
				}
				
				if(!channelsRemovedFrom.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					for(TextChannel tc: channelsRemovedFrom) {
						sb.append("- [#" + tc.getName() + "](https://discord.com/channels/" + gd.getId() + "/" + tc.getId() + ")\n");
						
					}
					eb.addField("Benachrichtigungen deaktiviert", sb.toString(), false);
				}
				
				if(!channelsUnchanged.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					for(TextChannel tc: channelsUnchanged) {
						sb.append("- [#" + tc.getName() + "](https://discord.com/channels/" + gd.getId() + "/" + tc.getId() + ")\n");
						
					}
					eb.addField("Keine Änderungen", sb.toString(), false);
				}
				
				//Test PN for later reuse
				message.getAuthor().openPrivateChannel().queue((privChannel) ->
		        {
		        	privChannel.sendMessage(eb.build()).queue();
		        	
		        });
				
				message.addReaction("👌").queue();
			}
			//nc.persistData();
		} else { // No channel was mentioned
			if(userMessage.length > 1) {
				switch (userMessage[1].toLowerCase()) {

				case "info":
					
					StringBuilder msgText = new StringBuilder();
					
					ArrayList<ChannelData> channels = nc.getNotificationChannelOfGuildForUser(gd.getId(), userID);
					if(!channels.isEmpty()) {
						msgText.append("Auf dem Server [" + gd.getName() + "](https://discord.com/channels/" + gd.getId() + ") erhältst du aktuell in folgenden Kanälen Updates: ");
						int counter = 1;
						for(ChannelData cd: channels) {
							msgText.append("[#" + cd.getName() + "](https://discord.com/channels/" + gd.getId() + "/" + cd.getId() + ")");
							if(counter < channels.size()) {
								msgText.append(", ");
							}
							counter++;
						}
					} else {
						msgText.append("Auf dem Server [" + gd.getName() + "](https://discord.com/channels/" + gd.getId() + ") erhältst du aktuell keine Kanal-Updates.");
					}
					
					EmbedBuilder eb = new EmbedBuilder().setTitle("Status - Benachrichtigungen über Kanal-Updates")
							.setColor(Antony.getBaseColor())
							.setThumbnail(channel.getGuild().getIconUrl())
							.setDescription(msgText.toString())
							.setFooter("Antony | Version " + Antony.getVersion());
					message.getAuthor().openPrivateChannel().queue((privChannel) ->
			        {
			        	privChannel.sendMessage(eb.build()).queue();
			        	
			        });
					
					break;
				case "off": // if user wants to receive no more notifications for this guild
						nc.removeFromAllListsOfGuild(gd.getId(), userID);
						channel.sendMessage("Du erhältst nun keine weiteren Benachrichtigungen mehr.").queue();
					break;
				default: // give the user an overview on which channels he'll receive notifications for this guild
						channel.sendMessage(getHelpText()).queue();
					break;
					
				}
			} else { //send help message because of no parameters
				channel.sendMessage(getHelpText()).queue();
			}
		}
		nc.persistData();
		
		
	}
	
	private String getHelpText() {
		StringBuilder msg = new StringBuilder();
		msg.append("Folgende Befehle stehen dir zur Verfügung:\n");
		msg.append("**" + Antony.getCmdPrefix() + "notify** - Gibt diese Hilfe aus.\n");
		msg.append("**" + Antony.getCmdPrefix() + "notify #kanal1 (#kanal2 ...)** - Aktiviert oder deaktiviert deine Benachrichtigungen für die Kanäle, abhängig von der aktuellen Einstellung\n");
		msg.append("**" + Antony.getCmdPrefix() + "notify on #kanal1 (#kanal2 ...)** - Aktiviert deine Benachrichtigungen für die Kanäle\n");
		msg.append("**" + Antony.getCmdPrefix() + "notify off #kanal1 (#kanal2 ...)** - Deaktiviert deine Benachrichtigungen für die Kanäle\n");
		msg.append("**" + Antony.getCmdPrefix() + "notify stats #kanal1 (#kanal2 ...)** - Zeigt, welche User den jeweiligen Kanal abonniert haben\n");
		msg.append("**" + Antony.getCmdPrefix() + "notify info** - Informiert dich über alle Kanäle, zu denen du aktuell über Updates benachrichtigt wirst.\n");
		msg.append("**" + Antony.getCmdPrefix() + "notify off** - Du wirst nicht mehr über Kanal-Updates informiert");
		return msg.toString();
	}
}