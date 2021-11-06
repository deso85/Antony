package bot.antony.events.reaction.add;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class MessageReaction {

	protected ReactionEmote emote;
	protected Guild guild;
	protected Message message;
	protected TextChannel responseChannel;	//Channel to respond to reaction
	protected Member reactor;
	protected StringBuilder logMessage = new StringBuilder();
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public MessageReaction(MessageReactionAddEvent event) {
		super();
		emote = event.getReactionEmote();
		guild = event.getGuild();
		this.message = event.retrieveMessage().complete();
		responseChannel = message.getTextChannel();
		reactor = event.getMember();
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public boolean shallTrigger() {
		return Antony.getGuildController().memberIsAdmin(reactor);
	}
	
	public void removeReaction() {
		message.removeReaction(emote.getName(), reactor.getUser()).queue();
	}
	
	public void mentionReactor() {
		responseChannel.sendMessage(emote.getName() + " " + reactor.getUser().getAsMention()).queue();
	}
	
	public void printAttachments() {
		if(message.getAttachments().size() > 0) {
			responseChannel.sendMessage("Folgende Attachments wurden gepostet:").complete();
			for(Attachment attachment : message.getAttachments()) {
				responseChannel.sendMessage(attachment.getUrl()).complete();
			}
		}
	}
	
	public void log() {
		if(logMessage.length() > 0) {
			Antony.getLogger().info(logMessage.toString());
		}
	}
	
	public void play() {
		
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	
}
