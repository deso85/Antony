package bot.antony.commands;

import java.util.ArrayList;

import bot.antony.Antony;
import bot.antony.commands.notification.NotificationController;
import bot.antony.commands.types.ServerCommand;
import bot.antony.guild.ChannelData;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class NotifyCmd extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public NotifyCmd() {
		super();
		this.privileged = false;
		this.name = "notify";
		this.description = "Kann genutzt werden, um über neue Einträge in Kanälen informiert zu werden. Nutze den Befehl, um detaillierte Informationen zur Handhabung zu bekommen.";
		this.shortDescription = "Kann genutzt werden, um über neue Einträge in Kanälen informiert zu werden.";
		this.example = "#channel";
		this.cmdParams.put("#channel (#channel2 ...)", "Aktiviert oder deaktiviert deine Benachrichtigungen für die Kanäle, abhängig von der aktuellen Einstellung.");
		this.cmdParams.put("on #channel (#channel2 ...)", "Aktiviert deine Benachrichtigungen für die Kanäle.");
		this.cmdParams.put("off #channel (#channel2 ...)", "Deaktiviert deine Benachrichtigungen für die Kanäle.");
		this.cmdParams.put("stats #channel (#channel2 ...)", "Zeigt, welche User den jeweiligen Kanal abonniert haben.");
		this.cmdParams.put("info", "Informiert dich über alle Kanäle, zu denen du aktuell über Updates benachrichtigt wirst.");
		this.cmdParams.put("off", "Du wirst nicht mehr über Kanal-Updates informiert.");
		this.cmdParams.put("now", "Administrative Funktion, um sofort alle ausstehenden Notifications zu versenden.");
	}
	
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		NotificationController nc = Antony.getNotificationController();
		GuildData guild = new GuildData(message.getGuild());
		UserData user = new UserData(message.getAuthor());

		// Subcommand with channel mention: (on|off|stats|statistics) #MentionedChannel
		if(message.getMentions().getChannels().size() > 0) {
			ArrayList<ChannelData> channelsAdddedTo = new ArrayList<ChannelData>();
			ArrayList<ChannelData> channelsRemovedFrom = new ArrayList<ChannelData>();
			ArrayList<ChannelData> channelsUnchanged = new ArrayList<ChannelData>();
			
			// Process each mentioned channel
			for(Channel mentionedChannel: message.getMentions().getChannels()) {
				if (mentionedChannel.getType().isMessage() || mentionedChannel.getType().isThread()) {
					ChannelData channelData = new ChannelData(mentionedChannel);
					Antony.getLogger().debug(channelData.toString());

					switch (userMessage[1].toLowerCase()) {

						case "on":
							// Enable notifications for the channel
							if (nc.addNotification(guild, channelData, user)) {
								channelsAdddedTo.add(channelData);
							} else {
								channelsUnchanged.add(channelData);
							}
							break;
						case "off":
							// Disable notifications for the channel
							if (nc.removeNotification(guild, channelData, user)) {
								channelsRemovedFrom.add(channelData);
							} else {
								channelsUnchanged.add(channelData);
							}
							break;
						case "stats":
						case "statistics":
							// Show which users have notifications enabled for this channel

							ArrayList<UserData> usrList = nc.getChannelUser(guild, channelData);
							StringBuilder msg = new StringBuilder();

							if (!usrList.isEmpty()) {
								msg.append("Folgende Benutzer werden über Aktualisierungen im Kanal " + mentionedChannel.getAsMention() + " informiert:\n");
								int counter = 1;
								for (UserData usr : usrList) {
									msg.append(message.getGuild().getMemberById(usr.getId()).getEffectiveName());
									if (counter < usrList.size()) {
										msg.append(", ");
										counter++;
									}
								}
								channel.sendMessage(msg.toString()).queue();
							} else {
								msg.append("Es wird aktuell niemand über Aktualisierungen im Kanal " + mentionedChannel.getAsMention() + " informiert.");
								channel.sendMessage(msg.toString()).queue();
							}

							break;
						default: // Toggle: enable if disabled, disable if enabled
							if (nc.toggleNotification(guild, channelData, user)) {
								channelsAdddedTo.add(channelData);
							} else {
								channelsRemovedFrom.add(channelData);
							}
							break;
					}
				}
			}
			
			if(!channelsAdddedTo.isEmpty() || !channelsRemovedFrom.isEmpty() || !channelsUnchanged.isEmpty()) {
				//Prepare message for PN
				EmbedBuilder eb = new EmbedBuilder().setTitle("Einstellungen - Benachrichtigungen über Kanal-Updates")
						.setColor(Antony.getBaseColor())
						.setThumbnail(channel.getGuild().getIconUrl())
						.setDescription("Folgende Änderungen wurden an den Einstellungen zur Benachrichtigung bei Kanal-Updates auf dem Server [" + guild.getName() + "](https://discord.com/channels/" + guild.getId() + ") durchgeführt.")
						.setFooter("Antony | Version " + Antony.getVersion());
				
				
				eb = addEbFields(guild, channelsAdddedTo, eb, "Benachrichtigungen aktiviert");
				eb = addEbFields(guild, channelsRemovedFrom, eb, "Benachrichtigungen deaktiviert");
				eb = addEbFields(guild, channelsUnchanged, eb, "Keine Änderungen");
				
				Utils.sendPM(message.getMember(), eb);
				
				message.addReaction(Emoji.fromUnicode("👌")).queue();
			}
			//nc.persistData();
		} else { // No channel was mentioned
			if(userMessage.length > 1) {
				switch (userMessage[1].toLowerCase()) {

				case "info":
					// Show all channels where the user receives notifications on this guild
					
					StringBuilder msgText = new StringBuilder();
					
					ArrayList<ChannelData> channels = nc.getNotificationChannelOfGuildForUser(guild, user);
					if(!channels.isEmpty()) {
						msgText.append("Auf dem Server [" + guild.getName() + "](https://discord.com/channels/" + guild.getId() + ") erhältst du aktuell in folgenden Kanälen Updates: ");
						int counter = 1;
						for(ChannelData channelData: channels) {
							msgText.append("[#" + channelData.getName() + "](https://discord.com/channels/" + guild.getId() + "/" + channelData.getId() + ")");
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
							.setThumbnail(channel.getGuild().getIconUrl())
							.setDescription(msgText.toString())
							.setFooter("Antony | Version " + Antony.getVersion());
					
					Utils.sendPM(message.getMember(), eb);
					
					break;
				case "off":
					// Disable all notifications for this guild
					nc.removeUserFromAllListsOfGuild(guild, user);
					channel.sendMessage("Du erhältst nun keine weiteren Benachrichtigungen mehr.").queue();
					break;
				case "now":
					// Subcommand-Permission: Admin-only, force-send all pending notifications
					if(member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
						nc.sendPendingNotifications();
						message.addReaction(Emoji.fromUnicode("👌")).queue();
					}
					break;
				default:
					// No recognized subcommand: show help
					printHelp(channel);
					break;
					
				}
			} else { //send help message because of no parameters
				printHelp(channel);
			}
		}
		nc.persistData();
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