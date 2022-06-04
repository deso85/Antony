package bot.antony.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.comparators.ChannelComparator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.channel.ChannelManager;

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
					List<net.dv8tion.jda.api.entities.Category> categories = guild.getCategoriesByName(catName, true);
					
					if(categories.size() > 0) {
						sort(categories.get(0));
						getChannel().sendMessage("Die Kanäle in der Kategorie **" + categories.get(0).getName() + "** wurden alphabetisch sortiert.").complete();
						
					} else {
						getChannel().sendMessage("Es wurde keine Kategorie mit dem Namen \"" + catName + "\" gefunden.").complete();
					}
				} else {
					printHelp();
				}
				break;
			case "sync":
				if (userMessage.length > 2) {
					String catName = message.getContentDisplay().substring(userMessage[0].length() + userMessage[1].length() + 2);
					List<net.dv8tion.jda.api.entities.Category> categories = guild.getCategoriesByName(catName, true);
					
					if(categories.size() > 0) {
						sync(categories.get(0));
						getChannel().sendMessage("Die Kanäle in der Kategorie **" + categories.get(0).getName() + "** wurden synchronisiert.").complete();
						
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
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "category (list | sort  [category name] | sync [channel name])").queue();
	}

	public static void sort(net.dv8tion.jda.api.entities.Category category) {
		if(category.getTextChannels().size() > 0) {
			category.modifyTextChannelPositions().sortOrder(new ChannelComparator()).queue();
		}
		if(category.getVoiceChannels().size() > 0) {
			category.modifyVoiceChannelPositions().sortOrder(new ChannelComparator()).queue();
		}
	}
	
	private void sync(net.dv8tion.jda.api.entities.Category category) {
		
		ArrayList<Permission> allow = new ArrayList<Permission>();
		//allow.add(Permission.MESSAGE_READ);
		allow.add(Permission.VIEW_CHANNEL);
		allow.add(Permission.MESSAGE_SEND);
		allow.add(Permission.MESSAGE_ATTACH_FILES);
		allow.add(Permission.MESSAGE_EMBED_LINKS);
		
		/*for(GuildChannel chan : category.getChannels()) {
			
			MessageHistory mh = new MessageHistory((MessageChannel) chan);
			mh.retrievePast(1).complete();

			if(mh.getRetrievedHistory().size() > 0) {
				getChannel().sendMessage(chan.getAsMention() + " -> " + mh.getRetrievedHistory().get(0).getAuthor().getName()).complete();
				chan.getPermissionContainer().getManager().putMemberPermissionOverride(mh.getRetrievedHistory().get(0).getAuthor().getIdLong(), allow, null).complete();
				//chan.getManager().sync().putMemberPermissionOverride(mh.getRetrievedHistory().get(0).getAuthor().getIdLong(), allow, null).complete();
			} else {
				getChannel().sendMessage(chan.getAsMention() + " -> NIEMAND").complete();
				chan.getManager().
				//chan.getManager().sync().complete();
			}
		}*/
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
