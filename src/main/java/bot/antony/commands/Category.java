package bot.antony.commands;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Category implements ServerCommand {

	private TextChannel channel;

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		setChannel(channel);

		String[] userMessage = message.getContentDisplay().split(" ");
		Guild guild = message.getGuild();
		
		if (userMessage.length > 1) {

			switch (userMessage[1].toLowerCase()) {
			case "list":
				if (userMessage.length > 2) {
					String catName = message.getContentDisplay().substring(userMessage[0].length() + userMessage[1].length() + 2);
					List<net.dv8tion.jda.api.entities.Category> categories = guild.getCategoriesByName(catName, false);
					
					if(categories.size() > 0) {
						StringBuilder categoryChannels = new StringBuilder();
						categoryChannels.append("Die Kategorie **" + catName + "** enthält folgende Kanäle:");
						for(GuildChannel chan : categories.get(0).getChannels()) {
							categoryChannels.append("\n" + chan.getAsMention());
						}
						getChannel().sendMessage(categoryChannels.toString()).complete();
					} else {
						getChannel().sendMessage("Es wurde keine Kategorie mit dem Namen \"" + catName + "\" gefunden.").complete();
					}
				} else {
					StringBuilder allCategories = new StringBuilder();
					allCategories.append("**Verfügbare Kategorien:**\n");
					for(net.dv8tion.jda.api.entities.Category cat : guild.getCategories()) {
						allCategories.append(cat.getAsMention() + "\n");
					}
					getChannel().sendMessage(allCategories.toString()).complete();
				}
				
				break;
			
			case "sort":
				if (userMessage.length > 2) {
					String catName = message.getContentDisplay().substring(userMessage[0].length() + userMessage[1].length() + 2);
					List<net.dv8tion.jda.api.entities.Category> categories = guild.getCategoriesByName(catName, false);
					
					if(categories.size() > 0) {
						int curPos = categories.get(0).getChannels().get(0).getPosition();
						GuildChannel topChan = null;

						TreeMap<String, GuildChannel> channels = new TreeMap<>();
						for(GuildChannel chan : categories.get(0).getChannels()) {
							if(chan.getName().contains("chat-hb")) {
								topChan = chan;
							} else {
								channels.put(chan.getName(), chan);
							}
						}

						if(topChan != null) {
							topChan.getManager().setPosition(curPos).complete();
							curPos++;
						}
						
						for (HashMap.Entry<String, GuildChannel> entry : channels.entrySet()) {
						    GuildChannel chan = entry.getValue();
						    chan.getManager().setPosition(curPos).complete();
						    curPos++;
						}
						
						getChannel().sendMessage("Die Kanäle in der Kategorie **" + catName + "** wurden alphabetisch sortiert.").complete();
						
					} else {
						getChannel().sendMessage("Es wurde keine Kategorie mit dem Namen \"" + catName + "\" gefunden.").complete();
					}
				} else {
					printHelp();
				}
				break;
			default:
				printHelp();
				break;
			}
		} else {
			printHelp();
		}
	}


	private void printHelp() {
		//TODO: Help ausformulieren
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "category (list | sort) [category name]").queue();
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