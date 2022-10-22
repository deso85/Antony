package bot.antony.events;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildVoiceUpdate extends ListenerAdapter {

	@Override
	public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
		StringBuilder logMessage = new StringBuilder();
		
		if(event.getMember() != null && !event.getMember().getOnlineStatus().equals(OnlineStatus.ONLINE)) {
			UserController usrCntrl = Antony.getUserController();
			UserData user = usrCntrl.loadUserData(event.getMember());
			user.setLastOnline(System.currentTimeMillis());
			usrCntrl.saveUserData(user, event.getGuild());
			logMessage.append("User [" + user.toString() + "] ");
			//moved channel
			if(event.getOldValue() != null && event.getNewValue() != null) {
				logMessage.append("moved from voice channel \"" + event.getChannelLeft().getName() + "\""
						+ " to voice channel \"" + event.getChannelJoined().getName() + "\".");
			} else if (event.getOldValue() == null) {
				//joined channel
				logMessage.append("joined voice channel \"" + event.getChannelJoined().getName() + "\".");
			} else {
				//left channel
				logMessage.append("left voice channel \"" + event.getChannelLeft().getName() + "\".");
			}
		}
		
	    Antony.getLogger().info(logMessage.toString());
	}
	
}
