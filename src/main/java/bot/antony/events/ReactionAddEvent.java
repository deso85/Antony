package bot.antony.events;

import bot.antony.events.reaction.add.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionAddEvent extends ListenerAdapter {
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
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
			case "redflag":
				return new RedFlagReaction(event);
			default:
				return null;
		}
	}
}
