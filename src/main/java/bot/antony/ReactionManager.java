package bot.antony;

import java.util.LinkedHashMap;

import bot.antony.events.reaction.add.AamProposalDecision;
import bot.antony.events.reaction.add.EggReaction;
import bot.antony.events.reaction.add.HammerReaction;
import bot.antony.events.reaction.add.ImageReaction;
import bot.antony.events.reaction.add.MessageReaction;
import bot.antony.events.reaction.add.MuteReaction;
import bot.antony.events.reaction.add.RedFlagReaction;
import bot.antony.events.reaction.add.SpyReaction;
import bot.antony.events.reaction.add.YellowSquareReaction;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class ReactionManager {

	public LinkedHashMap<String, MessageReaction> reactions = new LinkedHashMap<String, MessageReaction>();

	public ReactionManager() {
		reactions.put("🥚", new EggReaction());
		reactions.put("🕵️", new SpyReaction());
		reactions.put("🕵️‍♂️", new SpyReaction());
		reactions.put("🕵️‍♀️", new SpyReaction());
		reactions.put("🖼️", new ImageReaction());
		reactions.put("🔨", new HammerReaction());
		reactions.put("🟨", new YellowSquareReaction());
		//reactions.put("🔇", new MuteReaction());
		reactions.put("redflag", new RedFlagReaction());
		reactions.put("ausstehend", new AamProposalDecision());
		reactions.put("abgelehnt", new AamProposalDecision());
		reactions.put("akzeptiert", new AamProposalDecision());
		reactions.put("abgeschlossen", new AamProposalDecision());
	}

	public boolean perform(MessageReactionAddEvent event) {
		MessageReaction msgReaction;
		if ((msgReaction = this.reactions.get(event.getReactionEmote().getName())) != null) {
			msgReaction.perform(event);
			return true;
		}
		return false;
	}
	
	public LinkedHashMap<String, MessageReaction> getReactions(){
		return reactions;
	}
	
	public boolean hasReaction(String name) {
		return reactions.containsKey(name);
	}
	
	public MessageReaction getReaction(String name) {
		if(hasReaction(name)) {
			return reactions.get(name);
		}
		return null;
	}
}
