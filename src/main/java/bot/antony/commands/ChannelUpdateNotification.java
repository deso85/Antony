package bot.antony.commands;

import java.util.ArrayList;

import bot.antony.Antony;
import bot.antony.commands.notification.NotificationController;
import bot.antony.commands.types.ServerCommand;
import bot.antony.guild.GuildData;
import bot.antony.guild.channel.ChannelData;
import bot.antony.guild.user.UserData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class ChannelUpdateNotification implements ServerCommand {

	@Override
	public void performCommand(Member m, TextChannel cmdChannel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		NotificationController nc = Antony.getNotificationController();
		GuildData guild = new GuildData(message.getGuild());
		UserData user = new UserData(message.getAuthor());
		
		// Exp. Parameter: (on|off|stats|statistics) #MentionedChannel
		if(message.getMentionedChannels().size() > 0) {
			ArrayList<ChannelData> channelsAdddedTo = new ArrayList<ChannelData>();
			ArrayList<ChannelData> channelsRemovedFrom = new ArrayList<ChannelData>();
			ArrayList<ChannelData> channelsUnchanged = new ArrayList<ChannelData>();
			
			
			//for each mentioned channel...
			for(TextChannel textChannel: message.getMentionedChannels()) {
				ChannelData channel = new ChannelData(textChannel);
				Antony.getLogger().debug(channel.toString());
				
				switch (userMessage[1].toLowerCase()) {

				case "on": // If user wants to receive notifications for channel
						if(nc.addNotification(guild, channel, user)) {
							channelsAdddedTo.add(channel);
						} else {
							channelsUnchanged.add(channel);
						}
					break;
				case "off": // If user doesn't want to receive notifications for channel
						if(nc.removeNotification(guild, channel, user)) {
							channelsRemovedFrom.add(channel);
						} else {
							channelsUnchanged.add(channel);
						}
					break;
				case "stats":
				case "statistics": // If user want's to see who has notifications turned on for specified channel
						
						ArrayList<UserData> usrList = nc.getChannelUser(guild, channel);
						StringBuilder msg = new StringBuilder();
						
						if(!usrList.isEmpty()) {
							msg.append("Folgende Benutzer werden über Aktualisierungen im Kanal " + textChannel.getAsMention() + " informiert:\n");
							int counter = 1;
							for(UserData usr: usrList) {
								msg.append(message.getGuild().getMemberById(usr.getId()).getEffectiveName());
								if(counter < usrList.size()) {
									msg.append(", ");
									counter++;
								}
							}
							cmdChannel.sendMessage(msg.toString()).queue();
						} else {
							msg.append("Es wird aktuell niemand über Aktualisierungen im Kanal " + textChannel.getAsMention() + " informiert.");
							cmdChannel.sendMessage(msg.toString()).queue();
						}
						
					break;
				default: // Toggle - Turns on if user doesn't receive notifications and vice versa
						if(nc.toggleNotification(guild, channel, user)) {
							channelsAdddedTo.add(channel);
						} else {
							channelsRemovedFrom.add(channel);
						}
					break;
				}
			}
			
			if(!channelsAdddedTo.isEmpty() || !channelsRemovedFrom.isEmpty() || !channelsUnchanged.isEmpty()) {
				//Prepare message for PN
				EmbedBuilder eb = new EmbedBuilder().setTitle("Einstellungen - Benachrichtigungen über Kanal-Updates")
						.setColor(Antony.getBaseColor())
						.setThumbnail(cmdChannel.getGuild().getIconUrl())
						.setDescription("Folgende Änderungen wurden an den Einstellungen zur Benachrichtigung bei Kanal-Updates auf dem Server [" + guild.getName() + "](https://discord.com/channels/" + guild.getId() + ") durchgeführt.")
						.setFooter("Antony | Version " + Antony.getVersion());
				
				
				eb = addEbFields(guild, channelsAdddedTo, eb, "Benachrichtigungen aktiviert");
				eb = addEbFields(guild, channelsRemovedFrom, eb, "Benachrichtigungen deaktiviert");
				eb = addEbFields(guild, channelsUnchanged, eb, "Keine Änderungen");
				MessageEmbed messageEmbed = eb.build();
				//Test PN for later reuse
				message.getAuthor().openPrivateChannel().queue((privChannel) ->
		        {
		        	privChannel.sendMessage(messageEmbed).queue();
		        	
		        });
				
				message.addReaction("👌").queue();
			}
			//nc.persistData();
		} else { // No channel was mentioned
			if(userMessage.length > 1) {
				switch (userMessage[1].toLowerCase()) {

				case "info":
					
					StringBuilder msgText = new StringBuilder();
					
					ArrayList<ChannelData> channels = nc.getNotificationChannelOfGuildForUser(guild, user);
					if(!channels.isEmpty()) {
						msgText.append("Auf dem Server [" + guild.getName() + "](https://discord.com/channels/" + guild.getId() + ") erhältst du aktuell in folgenden Kanälen Updates: ");
						int counter = 1;
						for(ChannelData channel: channels) {
							msgText.append("[#" + channel.getName() + "](https://discord.com/channels/" + guild.getId() + "/" + channel.getId() + ")");
							if(counter < channels.size()) {
								msgText.append(", ");
								counter++;
							}
						}
					} else {
						msgText.append("Auf dem Server [" + guild.getName() + "](https://discord.com/channels/" + guild.getId() + ") erhältst du aktuell keine Kanal-Updates.");
					}
					//TODO Message won't be send if there are more than 1024 charakters. Has to be fixed for every pm
					EmbedBuilder eb = new EmbedBuilder().setTitle("Status - Benachrichtigungen über Kanal-Updates")
							.setColor(Antony.getBaseColor())
							.setThumbnail(cmdChannel.getGuild().getIconUrl())
							.setDescription(msgText.toString())
							.setFooter("Antony | Version " + Antony.getVersion());
					message.getAuthor().openPrivateChannel().queue((privChannel) ->
			        {
			        	privChannel.sendMessage(eb.build()).queue();
			        	
			        });
					
					break;
				case "off": // if user wants to receive no more notifications for this guild
						nc.removeUserFromAllListsOfGuild(guild, user);
						cmdChannel.sendMessage("Du erhältst nun keine weiteren Benachrichtigungen mehr.").queue();
					break;
				default: // give the user an overview on which channels he'll receive notifications for this guild
						cmdChannel.sendMessage(getHelpText()).queue();
					break;
					
				}
			} else { //send help message because of no parameters
				cmdChannel.sendMessage(getHelpText()).queue();
			}
		}
		nc.persistData();
		
		
	}
	
	private String getHelpText() {
		StringBuilder helpText = new StringBuilder();
		helpText.append("Folgende Befehle stehen dir zur Verfügung:\n");
		helpText.append("**" + Antony.getCmdPrefix() + "notify** - Gibt diese Hilfe aus.\n");
		helpText.append("**" + Antony.getCmdPrefix() + "notify #kanal1 (#kanal2 ...)** - Aktiviert oder deaktiviert deine Benachrichtigungen für die Kanäle, abhängig von der aktuellen Einstellung\n");
		helpText.append("**" + Antony.getCmdPrefix() + "notify on #kanal1 (#kanal2 ...)** - Aktiviert deine Benachrichtigungen für die Kanäle\n");
		helpText.append("**" + Antony.getCmdPrefix() + "notify off #kanal1 (#kanal2 ...)** - Deaktiviert deine Benachrichtigungen für die Kanäle\n");
		helpText.append("**" + Antony.getCmdPrefix() + "notify stats #kanal1 (#kanal2 ...)** - Zeigt, welche User den jeweiligen Kanal abonniert haben\n");
		helpText.append("**" + Antony.getCmdPrefix() + "notify info** - Informiert dich über alle Kanäle, zu denen du aktuell über Updates benachrichtigt wirst.\n");
		helpText.append("**" + Antony.getCmdPrefix() + "notify off** - Du wirst nicht mehr über Kanal-Updates informiert");
		return helpText.toString();
	}
	
	private EmbedBuilder addEbFields(GuildData guild, ArrayList<ChannelData> channels, EmbedBuilder eb, String title) {
		if(!channels.isEmpty()) {
			
			ArrayList<String> textList = new ArrayList<String>();
			StringBuilder fieldText = new StringBuilder();
			String textPart;
			int counter=1;
			
			for(ChannelData channel: channels) {
				textPart = "[#" + channel.getName() + "](https://discord.com/channels/" + guild.getId() + "/" + channel.getId() + ")";
				
				if((fieldText.length() + textPart.length() + 2) > 1024) {
					textList.add(fieldText.toString());
					fieldText = new StringBuilder();
				}
				
				fieldText.append(textPart);
				if(counter < channels.size()) {
					fieldText.append(", ");
					counter++;
				} else {
					textList.add(fieldText.toString());
				}
				
			}
			
			counter=1;
			for(String text: textList) {
				if(counter > 1) {
					title = "";
				}
				eb.addField(title, text, false);
				counter++;
			}
			
		}
		return eb;
	}
}