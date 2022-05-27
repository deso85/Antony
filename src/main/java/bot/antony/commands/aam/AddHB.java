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

		message.reply(
				"Ein Haltungsbericht sollte über einen längeren Zeitraum geführt, idealerweise in regelmäßigen Abständen aktualisiert werden und sowohl positive als auch negative Entwicklungen und mögliche Gründe enthalten.\nTraust du dir einen HB zu?")
				.queue(msg -> {
					msg.addReaction("✅").queue();
					msg.addReaction("❌").queue();
					message.getJDA().addEventListener(
							new AddHBStateMachine(channel, member.getUser(), message.getIdLong(), msg.getIdLong()));
				});

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
