package bot.antony.events.reaction.add;

import bot.antony.Antony;
import bot.antony.utils.Utils;
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
		if(responseChannel != null) {
			responseChannel.sendMessage(Utils.getAllUserInfos(message.getMember())).queue();
		}
	}
	
	@Override
	public void setVariables(MessageReactionAddEvent event) {
		super.setVariables(event);
		responseChannel = Antony.getGuildController().getLogChannel(event.getGuild());
	}
}
