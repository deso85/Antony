package bot.antony.commands.aam;

import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class AddHB implements ServerCommand {

	private TextChannel channel;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		setChannel(channel);

		message.reply("Zu welcher Ameisen-Art willst du einen Haltungsbericht führen?").queue();
		message.getJDA().addEventListener(new AddHBStateMachine(channel, member.getUser(), message.getIdLong()));
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
