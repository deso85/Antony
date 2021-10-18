package bot.antony.events;

import java.util.List;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.guild.UserData;
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
				UserController usrCntrl = Antony.getUserController();
				UserData user = usrCntrl.loadUserData(member);
				user.setName(event.getNewName());
				usrCntrl.saveUserData(user, guild);
			}
		}
	}
}
