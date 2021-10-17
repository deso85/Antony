package bot.antony.events;

import java.util.List;

import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserUpdateName extends ListenerAdapter {
	
	@Override
	public void onUserUpdateName(UserUpdateNameEvent event) {
		List<Guild> guilds = event.getJDA().getGuilds();
		
		for(Guild guild : guilds) {
			Member member = guild.getMember(event.getUser());
			if(member != null) {
				UserData user = Utils.loadUserData(member);
				user.setName(event.getNewName());
				user.getNames().put(System.currentTimeMillis(), event.getNewName());
				Utils.storeUserData(user, guild);
			}
		}
	}
}
