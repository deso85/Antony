package bot.antony.events;

import bot.antony.Antony;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMemberUpdateNickname extends ListenerAdapter {
	
	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
		UserData user = Utils.loadUserData(event.getMember());
		GuildData guild = new GuildData(event.getGuild());
		Long now = System.currentTimeMillis();
		
		//Add new nickname
		user.addNickname(now, event.getNewNickname());
		
		//Update last online (can't update nickname if not online)
		user.setLastOnline(now);
		
		//Save updated user data
		Utils.storeUserData(user, event.getGuild());
		
		//Log
		Antony.getLogger().info("Nickname of user " + user.toString() + " on guild " + guild.toString() + " changed to \"" + event.getNewNickname() + "\"");
	}
}
