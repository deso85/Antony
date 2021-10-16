package bot.antony.events;

import java.util.ArrayList;

import bot.antony.Antony;
import bot.antony.commands.notification.NotificationController;
import bot.antony.commands.notification.UserNotification;
import bot.antony.guild.ChannelData;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class NotificationListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		//check which channel ...
		if(event.isFromType(ChannelType.TEXT)) {
			NotificationController nc = Antony.getNotificationController();
			final Guild guild = event.getGuild();
			final TextChannel channel = event.getTextChannel();
			GuildData guildData = new GuildData(guild);
		    ChannelData channelData = new ChannelData(channel);
			
			//check if the guild has notification lists
			if(nc.hasGCNL(guildData)) {
				//check if there is a notification list for this channel
				if(nc.getGCNL(guildData).hasCNL(channelData)) {
					ArrayList<UserData> usrlist = nc.getGCNL(guildData).getCNL(channelData).getUserList();
					//for each user who wants to receive a notification...
					for(UserData user: usrlist) {
						ArrayList<UserNotification> tempUserNotifications = new ArrayList<UserNotification>(nc.getPendingUserNotifications());
						UserNotification userNotification = new UserNotification(user, guildData);
						userNotification.addChannel(channelData);
						//if there is no notification it gets added
						if(!tempUserNotifications.contains(userNotification)) {
							tempUserNotifications.add(userNotification);
							//nc.getPendingUserNotifications().add(userNotification);
						} else { //if there already is a notification
							for(UserNotification un: nc.getPendingUserNotifications()) {
								//if this is the user notification we have to add the channel
								if(un.equals(userNotification)) {
									tempUserNotifications.remove(un);
									un.addChannel(channelData);
									tempUserNotifications.add(un);
								}
							}
						}
						nc.setPendingUserNotifications(tempUserNotifications);
					}
					if(!usrlist.isEmpty()) {
						nc.persistData();
					}
				}
			}
		}
	}
}
