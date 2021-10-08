package bot.antony.events.reaction.add;

import bot.antony.Antony;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class ImageReaction extends MessageReaction {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ImageReaction(MessageReactionAddEvent event) {
		super(event);
		allowedRoles.add("Intermorphe");
		responseChannel = event.getGuild().getTextChannelById(Antony.getAntonyLogChannelId());
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
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
		responseChannel.sendMessage(sb.toString()).queue();
		responseChannel.sendMessage(message.getAuthor().getEffectiveAvatarUrl() + "?size=2048").queue();
	}
	
}
