package bot.antony.commands;

import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Shutdown extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public Shutdown() {
		super();
		this.privileged = true;
		this.name = "shutdown";
		this.description = "Mit diesem Befehl lässt sich Antony stoppen.";
		this.shortDescription = "Stoppt Antony.";
	}
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		channel.sendMessage("Shutting down ...").queue();
		member.getJDA().getPresence().setStatus(OnlineStatus.OFFLINE);
		member.getJDA().shutdown();
	}

}
