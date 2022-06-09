package bot.antony.events;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionAddEvent extends ListenerAdapter {
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {

		if(event.getMember() != null && !event.getMember().getOnlineStatus().equals(OnlineStatus.ONLINE)) {
			UserController usrCntrl = Antony.getUserController();
			UserData user = usrCntrl.loadUserData(event.getMember());
			user.setLastOnline(System.currentTimeMillis());
			usrCntrl.saveUserData(user, event.getGuild());
		}
		
		if(Antony.getReactionMan().perform(event)) {
			//Reaction is available and has been executed
		}
	}
}
