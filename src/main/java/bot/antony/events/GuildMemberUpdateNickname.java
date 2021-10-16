package bot.antony.events;

import java.io.File;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMemberUpdateNickname extends ListenerAdapter {
	
	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
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
		
		//Add new nickname
		user.addNickname(now, event.getNewNickname());
		
		//Update last online (can't update nickname if not online)
		user.setLastOnline(now);
		
		//Save updated user data
		Utils.storeJSONData(subfolder, fileName, user);
		
		//Log
		Antony.getLogger().info("Nickname of user " + user.toString() + " on guild " + guild.toString() + " changed to \"" + event.getNewNickname() + "\"");
	}
}
