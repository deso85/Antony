package bot.antony.events.reaction.add;

import bot.antony.Antony;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class AamProposalDecision extends MessageReaction {

	protected Boolean decision = false;
	protected Boolean done = false;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AamProposalDecision(MessageReactionAddEvent event, Boolean decision, Boolean done) {
		super(event);
		this.decision = decision;
		this.done = done;
	}
	
	public AamProposalDecision(MessageReactionAddEvent event, Boolean decision) {
		super(event);
		this.decision = decision;
	}
	
	public AamProposalDecision(MessageReactionAddEvent event) {
		super(event);
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void play() {
		if(shallTrigger()) {
			removeOtherEmotes();
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
			if(done) {
				proposalDecision.append("wir haben deinen Vorschlag (" + message.getJumpUrl() + ") umgesetzt. 👍🏻");
			} else {
				proposalDecision.append("dein Vorschlag (" + message.getJumpUrl() + ") wurde angenommen und wird demnächst umgesetzt.");
			}
		} else {
			proposalDecision.append("leider wurde dein Vorschlag (" + message.getJumpUrl() + ") abgelehnt. Teile trotzdem gerne weiterhin deine Ideen mit uns!");
		}
		
		guild.getTextChannelById(proposalDecisionChannel).sendMessage(proposalDecision.toString()).queue();
	}

	private Boolean unpin() {
		if(message.isPinned()) {
			message.unpin().queue();
			return true;
		}
		return false;
	}

	private void removeOtherEmotes() {
		if(guild.getEmotesByName("ausstehend", true).size() > 0) {
			message.clearReactions(guild.getEmotesByName("ausstehend", true).get(0)).queue();
		}
		
		if(done) {
			if(guild.getEmotesByName("akzeptiert", true).size() > 0) {
				message.clearReactions(guild.getEmotesByName("akzeptiert", true).get(0)).queue();
			}
			if(guild.getEmotesByName("abgelehnt", true).size() > 0) {
				message.clearReactions(guild.getEmotesByName("abgelehnt", true).get(0)).queue();
			}
		}
		if(decision && !done) {
			if(guild.getEmotesByName("abgeschlossen", true).size() > 0) {
				message.clearReactions(guild.getEmotesByName("abgeschlossen", true).get(0)).queue();
			}
			if(guild.getEmotesByName("abgelehnt", true).size() > 0) {
				message.clearReactions(guild.getEmotesByName("abgelehnt", true).get(0)).queue();
			}
		}
		if(!decision && !done) {
			if(guild.getEmotesByName("abgeschlossen", true).size() > 0) {
				message.clearReactions(guild.getEmotesByName("abgeschlossen", true).get(0)).queue();
			}
			if(guild.getEmotesByName("akzeptiert", true).size() > 0) {
				message.clearReactions(guild.getEmotesByName("akzeptiert", true).get(0)).queue();
			}
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
