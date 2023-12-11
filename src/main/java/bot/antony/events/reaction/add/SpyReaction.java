package bot.antony.events.reaction.add;

import bot.antony.Antony;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class SpyReaction extends MessageReaction {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public SpyReaction() {
		super();
		this.description = "Diese Reaction kann dazu verwendet werden, Benutzer-Informationen auszugeben.";
		this.shortDescription = "Reaction für die Ausgabe von User-Informationen.";
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void perform(MessageReactionAddEvent event) {
		setVariables(event);
		if(shallTrigger(event.getMember())) {
			removeReaction();
			mentionReactor();
			printUserinfo();
		}
	}
	
	public void printUserinfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("ID: " + message.getAuthor().getId() + "\n");
		sb.append("Tag: " + message.getAuthor().getAsTag().replace("|", "\\|").replace("_", "\\_").replace("*", "\\*") + "\n");
		sb.append("Name: " + message.getAuthor().getName().replace("|", "\\|").replace("_", "\\_").replace("*", "\\*"));
		if(message.getMember() != null && message.getMember().getNickname() != null) {
			sb.append("\nNickname: " + message.getMember().getNickname().replace("|", "\\|").replace("_", "\\_").replace("*", "\\*"));
		}
		if(responseChannel != null) {
			responseChannel.sendMessage(sb.toString()).queue();
		}
	}
	
	@Override
	public void setVariables(MessageReactionAddEvent event) {
		super.setVariables(event);
		responseChannel = Antony.getGuildController().getLogChannel(event.getGuild());
	}
}
