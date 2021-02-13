package bot.antony.events;

import java.util.ArrayList;

import bot.antony.Antony;
import bot.antony.commands.notification.NotificationController;
import bot.antony.commands.notification.UserNotification;
import bot.antony.guild.GuildData;
import bot.antony.guild.channel.ChannelData;
import bot.antony.guild.user.UserData;
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
					
					for(UserData user: usrlist) {
						UserNotification userNotification = new UserNotification(user, guildData);
						userNotification.addChannel(channelData);
						if(!nc.getPendingUserNotifications().contains(userNotification)) {
							nc.getPendingUserNotifications().add(userNotification);
						} else {
							for(UserNotification un: nc.getPendingUserNotifications()) {
								if(un.equals(userNotification)) {
									nc.getPendingUserNotifications().remove(un);
									un.addChannel(channelData);
									nc.getPendingUserNotifications().add(un);
								}
							}
						}
					}
					if(!usrlist.isEmpty()) {
						nc.persistData();
					}
				}
			}
		}
	}
}
