package bot.antony.commands;

import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ShutdownCmd extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ShutdownCmd() {
		super();
		this.privileged = true;
		this.name = "shutdown";
		this.description = "Mit diesem Befehl lässt sich Antony stoppen.";
		this.shortDescription = "Stoppt Antony.";
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		channel.sendMessage("Shutting down ...").queue();
		member.getJDA().getPresence().setStatus(OnlineStatus.OFFLINE);
		member.getJDA().shutdown();
	}

}
