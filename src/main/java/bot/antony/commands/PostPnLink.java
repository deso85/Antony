package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class PostPnLink implements ServerCommand {

	private TextChannel channel;

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		setChannel(channel);

		String[] userMessage = message.getContentDisplay().split(" ");

		if (userMessage.length > 1 && message.getMentionedChannels().size() > 0) {

			channel.sendMessage("Hier ist der Link zu deinem Kanal: <#" + message.getMentionedChannels().get(0).getId() + " >").queue();
			
		} else {
			printHelp();
		}

	}


	private void printHelp() {
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "pnlink #Kanal").queue();
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