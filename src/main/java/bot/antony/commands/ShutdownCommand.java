package bot.antony.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;

import net.dv8tion.jda.api.OnlineStatus;

@CommandInfo(
    name = "Shutdown",
    description = "Safely shuts down the bot."
)
public class ShutdownCommand extends Command {

	public ShutdownCommand() {
		this.name = "shutdown";
		this.help = "safely shuts off the bot";
        this.guildOnly = false;
        this.ownerCommand = true;
	}

	@Override
	protected void execute(CommandEvent event) {
		event.reply("Shutting down...");
		event.getJDA().getPresence().setStatus(OnlineStatus.OFFLINE);
		event.getJDA().shutdown();
	}
	
}
