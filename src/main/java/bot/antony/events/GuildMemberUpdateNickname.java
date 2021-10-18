package bot.antony.events;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMemberUpdateNickname extends ListenerAdapter {
	
	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
		UserController usrCntrl = Antony.getUserController();
		UserData user = usrCntrl.loadUserData(event.getMember());
		GuildData guild = new GuildData(event.getGuild());
		
		//Add new nickname
		user.setNickname(event.getNewNickname());
		
		//Update last online (can't update nickname if not online)
		user.setLastOnline(System.currentTimeMillis());
		
		//Save updated user data
		usrCntrl.saveUserData(user, event.getGuild());
		
		//Log
		Antony.getLogger().info("Nickname of user " + user.toString() + " on guild " + guild.toString() + " changed to \"" + event.getNewNickname() + "\"");
	}
}
