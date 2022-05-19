package bot.antony.events;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildVoiceMove extends ListenerAdapter {
	
	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		StringBuilder logMessage = new StringBuilder();
		
		if(event.getMember() != null && !event.getMember().getOnlineStatus().equals(OnlineStatus.ONLINE)) {
			UserController usrCntrl = Antony.getUserController();
			UserData user = usrCntrl.loadUserData(event.getMember());
			user.setLastOnline(System.currentTimeMillis());
			usrCntrl.saveUserData(user, event.getGuild());
			logMessage.append("User [" + user.toString() + "] ");
			logMessage.append("moved from voice channel \"" + event.getChannelLeft().getName() + "\""
					+ " to voice channel \"" + event.getChannelJoined().getName() + "\".");
		}
		
	    Antony.getLogger().info(logMessage.toString());
	}
	
}
