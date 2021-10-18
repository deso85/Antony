package bot.antony.controller;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class UserController {

	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public UserController() {
		super();
	}

	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public UserData loadUserData(Member member) {
		UserData user = new UserData();
		String subfolder = "guilds" + File.separator + member.getGuild().getId() + " - " + member.getGuild().getName() + File.separator + "user" + File.separator;
		String fileName = member.getId() + ".json";
		
		//Load user data if exists
		user = (UserData) Utils.loadJSONData(subfolder, fileName, new TypeReference<UserData>(){}, user);
		
		//If user is unknown
		if(user.getId() == null || user.getId() == "") {
			user = new UserData(member);
			saveUserData(user, member.getGuild());
		}
		
		return user;
	}
	
	public void saveUserData(UserData user, Guild guild) {
		String subfolder = "guilds" + File.separator + guild.getId() + " - " + guild.getName() + File.separator + "user" + File.separator;
		String fileName = user.getId() + ".json";
		
		Utils.saveJSONData(subfolder, fileName, user);
	}
	
	public void updateGuildMember(Member member) {
		updateGuildMember(member, false);
	}
	
	public void updateGuildMember(Member member, boolean force) {
		boolean save = force;
		UserData user = loadUserData(member);
		
		if(member.getOnlineStatus().equals(OnlineStatus.ONLINE) || member.getOnlineStatus().equals(OnlineStatus.DO_NOT_DISTURB) || member.getOnlineStatus().equals(OnlineStatus.IDLE)) {
			user.setLastOnline(System.currentTimeMillis());
			save = true;
		}
		
		if(user.setName(member.getUser().getName()) || user.setNickname(member.getNickname())) {
			save = true;
		}
		
		if(save) {
			saveUserData(user, member.getGuild());
		}
	}
	
	public void updateAllGuildMember(Guild guild) {
		updateAllGuildMember(guild, false);
	}
	
	public void updateAllGuildMember(Guild guild, boolean force) {
		List<Member> members = guild.getMembers();
		for(Member member : members) {
			updateGuildMember(member, force);
		}
	}
}
