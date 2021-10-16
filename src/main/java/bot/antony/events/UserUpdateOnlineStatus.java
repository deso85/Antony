package bot.antony.events;

import java.io.File;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserUpdateOnlineStatus extends ListenerAdapter {
	
	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
		UserData user = new UserData();
		GuildData guild = new GuildData(event.getGuild());
		Long now = System.currentTimeMillis();
		String subfolder = "guilds" + File.separator + guild.getId() + " - " + guild.getName() + File.separator + "user" + File.separator;
		String fileName = event.getMember().getId() + ".json";
		
		//Load user data if exists
		user = (UserData) Utils.loadJSONData(subfolder, fileName, new TypeReference<UserData>(){}, user);
		
		//If it is the first nickname change
		if(user.getId() == null || user.getId() == "") {
			user = new UserData(event.getUser());
		}
		
		//If user isn't online anymore
		if(event.getOldOnlineStatus().equals(OnlineStatus.ONLINE)) {
			user.setLastOnline(now);
		
			//Save updated user data
			Utils.storeJSONData(subfolder, fileName, user);
			
			//Log
			Antony.getLogger().info("User " + user.toString() + " isn't online anymore - updates user data.");
		}
	}
}
