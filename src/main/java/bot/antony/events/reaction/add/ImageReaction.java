package bot.antony.events.reaction.add;

import bot.antony.Antony;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class ImageReaction extends MessageReaction {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ImageReaction() {
		super();
		this.description = "Diese Reaction kann dazu verwendet werden, den Avatar (Profilbild) des Users auszugeben.";
		this.shortDescription = "Reaction für die Ausgabe des Avatars.";
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
		sb.append("Tag: " + message.getAuthor().getAsTag() + "\n");
		sb.append("Name: " + message.getAuthor().getName());
		if(message.getMember().getNickname() != null) {
			sb.append("\nNickname: " + message.getMember().getNickname());
		}
		if(responseChannel != null) {
			responseChannel.sendMessage(sb.toString()).queue();
			if(!message.getAuthor().getEffectiveAvatarUrl().equals(message.getMember().getEffectiveAvatarUrl())) {
				responseChannel.sendMessage("Avatar:").queue();
				responseChannel.sendMessage(message.getAuthor().getEffectiveAvatarUrl() + "?size=2048").queue();
				responseChannel.sendMessage("Serverspezifischer Avatar:").queue();
				responseChannel.sendMessage(message.getMember().getEffectiveAvatarUrl() + "?size=2048").queue();
			} else {
				responseChannel.sendMessage(message.getAuthor().getEffectiveAvatarUrl() + "?size=2048").queue();
			}
		}
	}
	
	@Override
	public void setVariables(MessageReactionAddEvent event) {
		super.setVariables(event);
		responseChannel = Antony.getGuildController().getLogChannel(event.getGuild());
	}
	
}
