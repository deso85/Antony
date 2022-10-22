package bot.antony.commands.lists;

import bot.antony.commands.types.ServerCommand;
import bot.antony.controller.ListController;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public abstract class ListCommand extends ServerCommand {

	protected TextChannel channel;
	protected String listName;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ListCommand() {
		super();
		listName = "Template-List";
		this.privileged = true;
		this.name = "list";
		this.description = "Dies ist eine Template-Liste.";
		this.shortDescription = description;
		this.example = "add Begriff";
		this.cmdParams.put("add textString", "Fügt der " + listName + " einen Text/Begriff hinzu.");
		this.cmdParams.put("remove textString", "Entfernt einen Text/Begriff von der " + listName + ".");
		this.cmdParams.put("list", "Gibt alle Einträge von der " + listName + " aus.");
		this.cmdParams.put("reload", "Lädt die " + listName + " neu in den Speicher.");
		this.cmdParams.put("clear", "Löscht die " + listName + ".");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		ListController controller = this.getInstance();
		this.channel = channel;
		
		String[] userMessage = message.getContentDisplay().split(" ");

		if (userMessage.length > 1) {
			switch (userMessage[1].toLowerCase()) {
			
			case "add":
				if(userMessage.length > 2) {
					String addStr = message.getContentDisplay().substring(getSubStrStart(userMessage[1])).toLowerCase();
					if(controller.add(addStr)) {
						channel.sendMessage("\"" + addStr + "\" zur " + listName + " hinzugefügt.").queue();
					} else {
						channel.sendMessage("\"" + addStr + "\" war bereits auf der " + listName + ".").queue();
					}
				} else {
					printHelp(channel);
				}
				break;
				
			case "remove":
				if(userMessage.length > 2) {
					String remStr = message.getContentDisplay().substring(getSubStrStart(userMessage[1])).toLowerCase();
					if(controller.remove(remStr)) {
						channel.sendMessage("\"" + remStr + "\" von der " + listName + " gelöscht.").queue();
					} else {
						channel.sendMessage("\"" + remStr + "\" war nicht auf der " + listName + ".").queue();
					}
				} else {
					printHelp(channel);
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
				
			case "reload":
				controller.initData();
				channel.sendMessage("Die Liste wurde mit " + controller.getList().size() + " Einträgen neu geladen.").queue();
				break;
				
			case "clear":
				controller.clear();
				channel.sendMessage(listName + " geleert.").queue();
				break;
				
			default:
				printHelp(channel);
				break;
			}
		} else {
			printHelp(channel);
		}
	}
	
	public abstract ListController getInstance();

	
	private int getSubStrStart(String parameter) {
		//cmd prefix + cmd name + space + parameter + space
		int subStrStart = 1 + this.name.length() + 1 + parameter.length() + 1;
		return subStrStart;
	}
	
}