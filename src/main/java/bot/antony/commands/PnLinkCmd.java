package bot.antony.commands;

import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class PnLinkCmd extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public PnLinkCmd() {
		super();
		this.privileged = false;
		this.name = "pnlink";
		this.description = "Mit diesem Befehl lässt sich die Vorlage für einen Link zu einem Kanal ausgeben, den man auch in einer PN nutzen kann.";
		this.shortDescription = "Befehl zur Ausgabe eines Kanal-Links.";
		this.example = "#channel";
		this.cmdParams.put("#channel", "Gibt einen Kanal-Link zu #channel aus.");
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		if (message.getMentions().getChannels().size() > 0) {
			channel.sendMessage("Hier ist der Link zu deinem Kanal *(Entferne das Leerzeichen vor \">\")*:\n"
					+ "<#" + message.getMentions().getChannels().get(0).getId() + " >").complete();
		} else {
			printHelp(channel);
		}
	}

}