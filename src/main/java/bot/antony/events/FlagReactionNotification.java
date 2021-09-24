package bot.antony.events;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bot.antony.Antony;
import bot.antony.guild.user.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class FlagReactionNotification extends ListenerAdapter {

	int deletedCount = 0;
	Date lastDeleted = null;
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		final Guild guild = event.getGuild();
		final User eventUser = event.getUser();
	    UserData userData = new UserData(eventUser);
		
		StringBuilder logMessage = new StringBuilder();
		
		if(event.getReactionEmote().getName().equals("redflag")) {
			Message message = event.retrieveMessage().complete();
			List<User> userList = event.getReaction().retrieveUsers().complete();
			
			logMessage.append("User [" + userData.toString() + "] REDFLAGGED "
					+ "message [" + message.getId() + "] "
					+ "in channel #" + message.getChannel().getName() + " (CID: " + message.getChannel().getId() + ").");
			
			StringBuilder sb = new StringBuilder();
			sb.append(event.getReactionEmote().getEmote().getAsMention() + " **" + userList.size() + "**\n");
			sb.append("Markiert von: ");
			
			int counter = 1;
			for(User user: event.getReaction().retrieveUsers().complete()) {
				if(eventUser.equals(user)) {
					sb.append("__");
				}
				sb.append(user.getAsTag() + " (" + user.getId() + ")");
				if(eventUser.equals(user)) {
					sb.append("__");
				}
				if(counter < userList.size()) {
					sb.append(", ");
					counter++;
				}
			}
			
			guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(sb.toString()).complete();
			
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			EmbedBuilder eb = new EmbedBuilder()
					.setColor(Color.red)
					.setAuthor(message.getAuthor().getAsTag() + " | ID: " + message.getAuthor().getId(), null, message.getAuthor().getAvatarUrl())
					.setDescription(message.getContentDisplay())
					.addField("#" + event.getChannel().getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + guild.getId() + "/" + event.getChannel().getId() + "/" + event.getMessageId() + ")**", false)
					.setFooter(formatter.format(date));
			guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(eb.build()).complete();
			
			
			//If there are 5 or more flags Antony will probably delete the message
			int flagsToDeleteMessage = 5;
			if(userList.size() >= flagsToDeleteMessage) {
				int userCount = 0;
				for(User user : userList) {
					List<String> blockedRoles = new ArrayList<String>();
					
					//Roles which won't be counted
					blockedRoles.add("Ei");
					blockedRoles.add("2nd 🎤");
					
					if(guild.getMember(user).getRoles().size() > 0 && !Utils.memberHasRole(guild.getMember(user), blockedRoles)) {
						userCount++;
					}
				}
				if(userCount >= flagsToDeleteMessage) {
					//If last delete is 15min ago the counter gets reset
					if(lastDeleted == null || lastDeleted.before(new Date(System.currentTimeMillis() - 900 * 1000)) ) {
						deletedCount = 0;
					}
					if(deletedCount < 3) {
						deletedCount++;
						lastDeleted = new Date(System.currentTimeMillis());
					
						if(message.getAttachments().size() > 0) {
							guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage("Folgende Attachments wurden gepostet:").complete();
							for(Attachment attachment : message.getAttachments()) {
								guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(attachment.getUrl()).complete();
							}
						}
						message.delete().complete();
						guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(":exclamation:" + flagsToDeleteMessage + " oder mehr berechtigte User haben die Nachricht markiert, weshalb sie entfernt wurde.").complete();
						logMessage.append("\nBecause there were " + flagsToDeleteMessage + " flaggs or more the message has been deleted.");
					} else {
						guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(":exclamation:" + flagsToDeleteMessage + " oder mehr berechtigte User haben die Nachricht markiert, sie wurde aber **nicht** gelöscht, weil zu viele Nachrichten in den letzten 15min gelöscht wurden. **Bitte dringend prüfen, ob jemand das System ausnutzt!**").complete();
						logMessage.append("\nBecause there were " + flagsToDeleteMessage + " flaggs or more the message should have been deleted but there are too many deleted messages within the last 15min.");
					}
				}
			}
			Antony.getLogger().info(logMessage.toString());
		}
	}
}
