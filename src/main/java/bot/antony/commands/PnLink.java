package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class PnLink implements ServerCommand {

	private TextChannel channel;

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {

		setChannel(channel);

		String[] userMessage = message.getContentDisplay().split(" ");

		if (userMessage.length > 1 && message.getMentions().getChannels().size() > 0) {

			channel.sendMessage("Hier ist der Link zu deinem Kanal: <#" + message.getMentions().getChannels().get(0).getId() + " >").complete();
			
		} else {
			printHelp();
		}

	}


	private void printHelp() {
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "pnlink #Kanal").complete();
	}

	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------

	public TextChannel getChannel() {
		return channel;
	}

	public void setChannel(TextChannel channel) {
		this.channel = channel;
	}
}