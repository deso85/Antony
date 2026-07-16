package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class BeerCmd extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public BeerCmd() {
		super();
		this.privileged = false;
		this.name = "beer";
		this.description = "Du möchtest ein Bier bestellen?";
		this.shortDescription = "Bier gefällig?";
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		
		Thread timerThread = new Thread() {
			public void run() {
				try {
					Message msg = message.reply("Ein Bier... kommt sofort!").complete();
					Thread.sleep(10000);	//10sec
					msg.editMessage("Hier, bitteschön. Ein erfrischendes Glas Wasser! 🥤").queue();
					message.addReaction(Emoji.fromUnicode("🥤")).queue();
				} catch (InterruptedException e) {
					// Thread was interrupted (e.g. during restart) — ignore
					Thread.currentThread().interrupt();
				}
			}
		};
		timerThread.setName("beer-timer");
		Antony.registerTimerThread(timerThread);
		timerThread.start();
		
	}

}