package bot.antony.events.reaction.add;

import bot.antony.Antony;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class AamProposalDecision extends MessageReaction {

	protected Boolean decision;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AamProposalDecision(MessageReactionAddEvent event, Boolean decision) {
		super(event);
		this.decision = decision;
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void play() {
		if(shallTrigger()) {
			removePendingEmote();
			if(unpin()) {
				sendInfoMessage();
			}
		}
	}
	
	private void sendInfoMessage() {
		long proposalDecisionChannel = 778960516101046302L;
		if(Antony.isProdStage()) {
			proposalDecisionChannel = 543512436343308294L;
		}
		
		StringBuilder proposalDecision = new StringBuilder();
		proposalDecision.append("Hey " + message.getAuthor().getAsMention() + ", ");
		if(decision) {
			proposalDecision.append("wir freuen uns, dass dein Vorschlag (" + message.getJumpUrl() + ") angenommen und ggf. schon umgesetzt wurde. Weiter so! 👍🏻");
		} else {
			proposalDecision.append("leider wurde dein Vorschlag (" + message.getJumpUrl() + ") abgelehnt. Aber lass dich nicht entmutigen und teile gerne weiterhin deine tollen Ideen mit uns!");		}
		
		guild.getTextChannelById(proposalDecisionChannel).sendMessage(proposalDecision.toString()).queue();
	}

	private Boolean unpin() {
		if(message.isPinned()) {
			message.unpin().queue();
			return true;
		}
		return false;
	}

	private void removePendingEmote() {
		if(guild.getEmotesByName("ausstehend", true).size() > 0) {
			message.clearReactions(guild.getEmotesByName("ausstehend", true).get(0)).queue();
		}
	}

	@Override
	public boolean shallTrigger() {
		if(Antony.getGuildController().memberIsAdmin(reactor)) {
			return true;
		}
		return false;
	}
	
}
