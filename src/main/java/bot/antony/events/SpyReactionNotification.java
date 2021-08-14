package bot.antony.events;

import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SpyReactionNotification extends ListenerAdapter {

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		final Guild guild = event.getGuild();
		
		List<String> spys = new ArrayList<String>();
		spys.add("🕵️");
		spys.add("🕵️‍♂️");
		spys.add("🕵️‍♀️");
		
		if(spys.contains(event.getReactionEmote().getName())) {	//detective emoji
			Message message = event.retrieveMessage().complete();
			Member member = guild.getMember(event.getUser());
			List<String> allowedRoles = new ArrayList<String>();
			
			//Roles which may use the command
			allowedRoles.add("Admin");
			allowedRoles.add("Soldat");
			allowedRoles.add("Intermorphe");
			
			boolean mayUse = false;
			for(Role role: member.getRoles()) {
				if(allowedRoles.contains(role.getName())) {
					mayUse = true;
				}
			}

			if(mayUse) {
				message.removeReaction(event.getReactionEmote().getName(), event.getUser()).queue();
				guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage("🕵️ " + event.getUser().getAsMention()).queue();
				StringBuilder sb = new StringBuilder();
				sb.append("ID: " + message.getAuthor().getId() + "\n");
				sb.append("Tag: " + message.getAuthor().getAsTag() + "\n");
				sb.append("Name: " + message.getAuthor().getName());
				if(message.getMember().getNickname() != null) {
					sb.append("\nNickname: " + message.getMember().getNickname());
				}
				guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(sb.toString()).queue();
			}
		}
	}
	
	public Role findRole(Member member, String name) {
	    List<Role> roles = member.getRoles();
	    return roles.stream()
	                .filter(role -> role.getName().equals(name)) // filter by role name
	                .findFirst() // take first result
	                .orElse(null); // else return null
	}
}
