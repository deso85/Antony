package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class UserCmd extends ServerCommand {
	
	GuildMessageChannel channel;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public UserCmd() {
		super();
		this.privileged = true;
		this.name = "user";
		this.description = "Der Befehl kann zur Administration von Usern genutzt werden.";
		this.shortDescription = "Befehl zur Administration von Usern.";
		this.example = "updateall force";
		this.cmdParams.put("updateall (force)", "Kann genutzt werden, um alle gespeicherten Benutzerdaten zu aktualisieren.");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		this.channel = channel;
		
		String[] userMessage = message.getContentDisplay().split(" ");
		if (userMessage.length > 1) {
			StringBuilder returnMessage = new StringBuilder();
			
			switch (userMessage[1].toLowerCase()) {
				case "updateall":
					boolean force = false;
					if(userMessage.length > 2 && userMessage[2].toLowerCase().equals("force")) {
						force = true;
					}
					Antony.getUserController().updateAllGuildMember(channel.getGuild(), force);
					returnMessage.append("Alle Benutzerdaten wurden gespeichert.");
					break;
				default:
					printHelp(channel);
					break;
			}
			
			if(returnMessage.length() > 0) {
				channel.sendMessage(returnMessage.toString()).queue();
			}
		} else {
			printHelp(channel);
		}
	}

}
