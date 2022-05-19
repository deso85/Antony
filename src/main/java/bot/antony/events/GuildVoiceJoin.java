package bot.antony.events;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildVoiceJoin extends ListenerAdapter {

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		StringBuilder logMessage = new StringBuilder();
		
		if(event.getMember() != null && !event.getMember().getOnlineStatus().equals(OnlineStatus.ONLINE)) {
			UserController usrCntrl = Antony.getUserController();
			UserData user = usrCntrl.loadUserData(event.getMember());
			user.setLastOnline(System.currentTimeMillis());
			usrCntrl.saveUserData(user, event.getGuild());
			logMessage.append("User [" + user.toString() + "] ");
			logMessage.append("joined voice channel \"" + event.getChannelJoined().getName() + "\".");
		}
		
	    Antony.getLogger().info(logMessage.toString());
	}
	
}
