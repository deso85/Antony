package bot.antony.events;

import bot.antony.Antony;
import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserUpdateOnlineStatus extends ListenerAdapter {
	
	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
		UserData user = Utils.loadUserData(event.getMember());
		Long now = System.currentTimeMillis();
		
		//If user isn't online anymore
		if(event.getOldOnlineStatus().equals(OnlineStatus.ONLINE) ||
				event.getOldOnlineStatus().equals(OnlineStatus.IDLE) ||
				event.getOldOnlineStatus().equals(OnlineStatus.DO_NOT_DISTURB)) {
			user.setLastOnline(now);
		
			//Save updated user data
			Utils.storeUserData(user, event.getGuild());
			
			//Log
			Antony.getLogger().debug("User " + user.toString() + " isn't online anymore - updates user data.");
		}
	}
}
