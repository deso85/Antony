package bot.antony.events;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.events.reaction.add.*;
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
		
		MessageReaction reaction = getReaction(event);
		if (reaction != null) {
			reaction.play();
		}
	}
	
	private MessageReaction getReaction(MessageReactionAddEvent event) {
		switch (event.getReactionEmote().getName()) {
			case "🥚":	//egg emoji :egg:
				return new EggReaction(event);
			case "🕵️":
				return new SpyReaction(event);
			case "🕵️‍♂️":
				return new SpyReaction(event);
			case "🕵️‍♀️":
				return new SpyReaction(event);
			case "🖼️":
				return new ImageReaction(event);
			case "🔨":
				return new HammerReaction(event);
			case "redflag":
				return new RedFlagReaction(event);
			default:
				return null;
		}
	}
}
