package bot.antony.commands.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.controller.ListController;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public abstract class ListCommand implements ServerCommand {

	protected TextChannel channel;
	protected List<String> allowedRoles = new ArrayList<>(Arrays.asList("Admin", "Soldat")); //Roles which may use the command
	protected String listName;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		ListController controller = this.getInstance();
		this.channel = channel;
		
		if(Utils.memberHasRole(member, allowedRoles)) {
	
			String[] userMessage = message.getContentDisplay().split(" ");
	
			if (userMessage.length > 1) {
	
				switch (userMessage[1].toLowerCase()) {
	
				case "add":
					if(userMessage.length > 2) {
						if(controller.add(userMessage[2].toLowerCase())) {
							channel.sendMessage("\"" + userMessage[2] + "\" zur " + listName + " hinzugefügt.").queue();
						} else {
							channel.sendMessage("\"" + userMessage[2] + "\" war bereits auf der " + listName + ".").queue();
						}
					} else {
						printHelp();
					}
					break;
					
				case "remove":
					if(userMessage.length > 2) {
						if(controller.remove(userMessage[2].toLowerCase())) {
							channel.sendMessage("\"" + userMessage[2] + "\" von der " + listName + " gelöscht.").queue();
						} else {
							channel.sendMessage("\"" + userMessage[2] + "\" war nicht auf der " + listName + ".").queue();
						}
					} else {
						printHelp();
					}
					break;
					
				case "list":
					StringBuilder sb = new StringBuilder();
					if(controller.getList().size() > 0) {
						sb.append("Folgende Ausdrücke sind auf der " + listName + ":\n");
					}
					sb.append(controller.list());
					channel.sendMessage(sb.toString()).queue();
					break;
					
				case "clear":
					controller.clear();
					channel.sendMessage(listName + " geleert.").queue();
					break;
					
				default:
					printHelp();
					break;
				}
			} else {
				printHelp();
			}
		}

	}
	
	public abstract ListController getInstance();

	private void printHelp() {
		channel.sendMessage("Benutzung: " + Antony.getCmdPrefix() + listName.toLowerCase() + " (add|remove|list|clear) [TextString]").queue();
	}
	
}