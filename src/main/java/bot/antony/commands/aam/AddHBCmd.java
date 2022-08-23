package bot.antony.commands.aam;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class AddHBCmd extends ServerCommand {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AddHBCmd() {
		super();
		this.privileged = false;
		this.name = "addhb";
		this.description = "Mit diesem Befehl lassen sich Kanäle für Haltungsberichte beantragen.";
		this.shortDescription = "Beantragung von Kanälen für Haltungsberichte.";
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		Antony.getLogger().info("HB dialogue started");
		message.reply(
			"Ein Haltungsbericht sollte über einen längeren Zeitraum geführt, idealerweise in regelmäßigen Abständen aktualisiert werden und sowohl positive als auch negative Entwicklungen und mögliche Gründe enthalten.\nTraust du dir einen HB zu?")
			.queue(msg -> {
				msg.addReaction(Emoji.fromUnicode("✅")).queue();
				msg.addReaction(Emoji.fromUnicode("❌")).queue();
				message.getJDA().addEventListener(
						new AddHBStateMachine(channel, member.getUser(), message.getIdLong(), msg.getIdLong()));
			});
	}

}
