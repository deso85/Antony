package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.IServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Mute implements IServerCommand {

	private TextChannel channel;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		setChannel(channel);

		
	}
	
	private void printHelp() {
		//TODO: Help ausformulieren
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "mute (Nickname|ID) [Time] [Reason]\nBeispiel: " + Antony.getCmdPrefix() + "mute Antony 1d Böser Bot!").queue();
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
