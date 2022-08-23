package bot.antony.events.reaction.add;

import java.util.Arrays;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class AamProposalDecision extends MessageReaction {

	protected Boolean decision = false;
	protected Boolean done = false;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AamProposalDecision() {
		super();
		this.description = "Diese Reaction kann dazu verwendet werden, um Vorschläge aus dem Vorschlagswesen zu bearbeiten.";
		this.shortDescription = "Reaction für das Vorschlagswesen.";
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void perform(MessageReactionAddEvent event) {
		setVariables(event);
		if(shallTrigger(event.getMember())) {
			removeOtherEmotes(event.getEmoji().getName());
			togglePin(event.getEmoji().getName());
		}
	}
	
	private void sendInfoMessage() {
		//TODO: Has to be rewritten because boolean variables are no longer set
		TextChannel proposalDecisionChannel = Antony.getGuildController().getValidChannel(guild, Arrays.asList(543512436343308294L, 778960516101046302L)); //Prod, Test
		
		if(proposalDecisionChannel != null) {
			StringBuilder proposalDecision = new StringBuilder();
			proposalDecision.append("Hey " + message.getAuthor().getAsMention() + ", ");
			if(decision) {
				if(done) {
					proposalDecision.append("wir haben deinen Vorschlag (" + message.getJumpUrl() + ") umgesetzt. 👍🏻");
				} else {
					proposalDecision.append("dein Vorschlag (" + message.getJumpUrl() + ") wurde angenommen und wird demnächst umgesetzt.");
				}
			} else {
				proposalDecision.append("leider wurde dein Vorschlag (" + message.getJumpUrl() + ") abgelehnt. Teile trotzdem gerne weiterhin deine Ideen mit uns!");
			}
			
			proposalDecisionChannel.sendMessage(proposalDecision.toString()).queue();
		}
	}

	private void togglePin(String reactionName) {
		switch(reactionName) {
			case "ausstehend":
				if(!message.isPinned()) {
					message.pin().queue();
				}
				break;
			default:
				if(message.isPinned()) {
					message.unpin().queue();
				}
				break;
		}
	}

	private void removeOtherEmotes(String reactionName) {
		switch (reactionName) {
		case "ausstehend":
			removeEmote("abgelehnt");
			removeEmote("akzeptiert");
			removeEmote("abgeschlossen");
			break;
		case "abgelehnt":
			removeEmote("ausstehend");
			removeEmote("akzeptiert");
			removeEmote("abgeschlossen");
			break;
		case "akzeptiert":
			removeEmote("ausstehend");
			removeEmote("abgelehnt");
			removeEmote("abgeschlossen");
			break;
		case "abgeschlossen":
			removeEmote("abgelehnt");
			removeEmote("ausstehend");
			removeEmote("akzeptiert");
			break;
		default:
			break;
		}
	}
	
	private void removeEmote(String reactionName) {
		if(guild.getEmojisByName(reactionName, true).size() > 0) {
			message.clearReactions(guild.getEmojisByName(reactionName, true).get(0)).queue();
		}
	}
}
