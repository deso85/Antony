package bot.antony.events.reaction.add;

import bot.antony.Antony;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class SpyReaction extends MessageReaction {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public SpyReaction(MessageReactionAddEvent event) {
		super(event);
		responseChannel = Antony.getGuildController().getLogChannel(event.getGuild());
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public boolean shallTrigger() {
		return Antony.getGuildController().memberIsMod(reactor);
	}
	
	@Override
	public void play() {
		if(shallTrigger()) {
			removeReaction();
			mentionReactor();
			printUserinfo();
		}
	}
	
	public void printUserinfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("ID: " + message.getAuthor().getId() + "\n");
		sb.append("Tag: " + message.getAuthor().getAsTag() + "\n");
		sb.append("Name: " + message.getAuthor().getName());
		if(message.getMember().getNickname() != null) {
			sb.append("\nNickname: " + message.getMember().getNickname());
		}
		if(responseChannel != null) {
			responseChannel.sendMessage(sb.toString()).queue();
		}
	}
}
