package bot.antony.events;

import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ImageReactionNotification extends ListenerAdapter {

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		final Guild guild = event.getGuild();
		
		List<String> avatars = new ArrayList<String>();
		avatars.add("🖼️");
		
		if(avatars.contains(event.getReactionEmote().getName())) {	//detective emoji
			Message message = event.retrieveMessage().complete();
			Member member = guild.getMember(event.getUser());
			List<String> allowedRoles = new ArrayList<String>();
			
			//Roles which may use the command
			allowedRoles.add("Admin");
			allowedRoles.add("Soldat");
			allowedRoles.add("Intermorphe");

			if(Utils.memberHasRole(member, allowedRoles)) {
				message.removeReaction(event.getReactionEmote().getName(), event.getUser()).queue();
				guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage("🖼️ " + event.getUser().getAsMention()).queue();
				StringBuilder sb = new StringBuilder();
				sb.append("ID: " + message.getAuthor().getId() + "\n");
				sb.append("Tag: " + message.getAuthor().getAsTag() + "\n");
				sb.append("Name: " + message.getAuthor().getName());
				if(message.getMember().getNickname() != null) {
					sb.append("\nNickname: " + message.getMember().getNickname());
				}
				guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(sb.toString()).queue();
				guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(message.getAuthor().getEffectiveAvatarUrl() + "?size=2048").queue();
			}
		}
	}
}
