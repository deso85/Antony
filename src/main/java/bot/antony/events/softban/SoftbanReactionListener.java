package bot.antony.events.softban;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bot.antony.Antony;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SoftbanReactionListener extends ListenerAdapter {

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		final Guild guild = event.getGuild();
		
		if(event.getReactionEmote().getName().equals("🔨")) {
			Message message = event.retrieveMessage().complete();
			Member member = guild.getMember(event.getUser());
			List<String> allowedRoles = new ArrayList<String>();
			
			//Roles which may use the command
			allowedRoles.add("Admin");
			allowedRoles.add("Soldat");
			allowedRoles.add("Intermorphe");

			if(Utils.memberHasRole(member, allowedRoles)) {
				message.removeReaction(event.getReactionEmote().getName(), event.getUser()).queue();
				UserDataSB user = new UserDataSB(message.getAuthor().getId(), message.getAuthor().getName());
				Antony.getSoftbanController().ban(user);
				guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage("🔨 User soft banned by " + event.getUser().getAsMention()).queue();
				Date date = new Date(System.currentTimeMillis());
				SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				EmbedBuilder eb = new EmbedBuilder()
						.setColor(Color.red)
						.setAuthor(message.getAuthor().getAsTag() + " | ID: " + message.getAuthor().getId(), null, message.getAuthor().getAvatarUrl())
						.setDescription(message.getContentDisplay())
						.addField("#" + event.getChannel().getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + guild.getId() + "/" + event.getChannel().getId() + "/" + event.getMessageId() + ")**", false)
						.setFooter(formatter.format(date));
				guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(eb.build()).queue();
			}
		}
	}
}
