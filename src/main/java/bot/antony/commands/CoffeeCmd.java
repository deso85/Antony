package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class CoffeeCmd extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public CoffeeCmd() {
		super();
		this.privileged = false;
		this.name = "coffee";
		this.description = "Du möchtest einen Kaffee bestellen?";
		this.shortDescription = "Kaffee gefällig?";
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		Thread timerThread = new Thread() {
			public void run() {
				try {
					Message msg = message.reply("Ein Kaffee... kommt sofort!").complete();
					Thread.sleep(10000);	//10sec
					msg.editMessage("Hier, bitteschön. Frisch gebrüht! ☕\n\nDenk aber daran, auch mal ein Glas Wasser zu trinken.").queue();
					message.addReaction(Emoji.fromUnicode("☕")).queue();
				} catch (InterruptedException e) {
					Antony.getLogger().error("Wasn't able to put Thread asleep.", e);
				}
			}
		};
		timerThread.start();
		
	}

}