package bot.antony.commands;


import java.util.Map.Entry;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Help extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public Help() {
		super();
		this.privileged = false;
		this.name = "help";
		this.description = "Mit diesem Befehl lassen sich alle verfügbaren Befehle oder einen Hilfetext zu einem spezifischen Befehlen an.";
		this.shortDescription = "Zeigt alle verfügbaren Befehle oder den Hilfetext zu einem Befehl an.";
		this.example = Antony.getCmdPrefix() + name + " antony";
		this.cmdParams.put("cmdName", "Zeigt die Hilfe für einen Befehl an.");
	}
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		if (userMessage.length > 1) {
			if(Antony.getCmdMan().getAvailableCommands(member).get(userMessage[1].toLowerCase()) != null) {
				Antony.getCmdMan().getAvailableCommands(member).get(userMessage[1].toLowerCase()).printHelp(channel);
			} else {
				channel.sendMessage("Der Befehl \"" + userMessage[1] + "\" ist nicht verfügbar.").queue();
			}
		} else {
			printHelp(channel, member);
		}
	}
	
	public void printHelp(TextChannel channel, Member member) {
		StringBuilder helptext = new StringBuilder();
		helptext.append("Folgende Befehle stehen dir zur Verfügung:\n");
		for(Entry<String, ServerCommand> entry : Antony.getCmdMan().getAvailableCommands(member).entrySet()) {
			helptext.append("**" + Antony.getCmdPrefix() + entry.getKey() + "**");
			if(entry.getValue().getShortDescription() != null && !entry.getValue().getShortDescription().isEmpty()) {
				helptext.append(" - " + entry.getValue().getShortDescription());
			}
			helptext.append("\n");
		}
		helptext.append("\nHilfe zu einem Befehl erhältst du, wenn du diesen Befehl nutzt:\n**"
				+ Antony.getCmdPrefix() + name + " cmdName**");
		channel.sendMessage(helptext).queue();
	}

}