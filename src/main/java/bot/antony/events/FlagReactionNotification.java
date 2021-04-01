package bot.antony.events;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import bot.antony.Antony;
import bot.antony.guild.user.UserData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class FlagReactionNotification extends ListenerAdapter {

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
			
			guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(sb.toString()).queue();
			
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			EmbedBuilder eb = new EmbedBuilder()
					.setColor(Color.red)
					.setAuthor(message.getAuthor().getAsTag() + " | ID: " + message.getAuthor().getId(), null, message.getAuthor().getAvatarUrl())
					.setDescription(message.getContentDisplay())
					.addField("#" + event.getChannel().getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + guild.getId() + "/" + event.getChannel().getId() + "/" + event.getMessageId() + ")**", false)
					.setFooter(formatter.format(date));
			guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(eb.build()).queue();
			
			Antony.getLogger().info(logMessage.toString());
		}
	}
}
