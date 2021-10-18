package bot.antony.events;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserUpdateOnlineStatus extends ListenerAdapter {
	
	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
		UserController usrCntrl = Antony.getUserController();
		UserData user = usrCntrl.loadUserData(event.getMember());
		
		//If user isn't online anymore
		if(event.getOldOnlineStatus().equals(OnlineStatus.ONLINE) ||
				event.getOldOnlineStatus().equals(OnlineStatus.IDLE) ||
				event.getOldOnlineStatus().equals(OnlineStatus.DO_NOT_DISTURB)) {
			user.setLastOnline(System.currentTimeMillis());
		
			//Save updated user data
			usrCntrl.saveUserData(user, event.getGuild());
			
			//Log
			Antony.getLogger().debug("User " + user.toString() + " isn't online anymore - updates user data.");
		}
	}
}
