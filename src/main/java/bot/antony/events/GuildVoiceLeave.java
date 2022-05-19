package bot.antony.events;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildVoiceLeave extends ListenerAdapter {
	
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		StringBuilder logMessage = new StringBuilder();
		
		if(event.getMember() != null && !event.getMember().getOnlineStatus().equals(OnlineStatus.ONLINE)) {
			UserController usrCntrl = Antony.getUserController();
			UserData user = usrCntrl.loadUserData(event.getMember());
			user.setLastOnline(System.currentTimeMillis());
			usrCntrl.saveUserData(user, event.getGuild());
			logMessage.append("User [" + user.toString() + "] ");
			logMessage.append("left voice channel \"" + event.getChannelLeft().getName() + "\".");
		}
		
	    Antony.getLogger().info(logMessage.toString());
	}
	
}
