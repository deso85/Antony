package bot.antony.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bot.antony.commands.types.ServerCommand;
import bot.antony.comparators.ChannelComparator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class CategoryCmd extends ServerCommand {

	private TextChannel channel;

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public CategoryCmd() {
		super();
		this.privileged = true;
		this.name = "category";
		this.description = "Der Befehl kann zur Administration von Server-Kategorien genutzt werden.";
		this.shortDescription = "Befehl zur Administration von Server-Kategorien.";
		this.example = "sort CategoryName";
		this.cmdParams.put("list (CategoryName)", "Kann genutzt werden, um alle Kategorien oder alle Kanäle innerhalb einer Kategorie auszugeben.");
		this.cmdParams.put("sort CategoryName", "Kann genutzt werden, um Kanäle innerhalb einer Kategorie alphabetisch zu sortieren.");
		this.cmdParams.put("sync CategoryName (-o | --owner)", "Kann genutzt werden, um die Berechtigungen der Kanäle innerhalb einer Kategorie mit den Berechtigungen der Kategorie zu synchronisieren. Der Parameter `-o` bzw `--owner` muss verwendet werden, wenn die Kanäle einen dedizierten Owner haben, der extra Berechtiguengen erhalten soll. Die letzte Person, die in einem Kanal geschrieben hat, erhält dann die nötigen Rechte.");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		this.channel = channel;
		String[] userMessage = message.getContentDisplay().split(" ");
		List<String> usrMsgList = Arrays.asList(userMessage);
		
		if (userMessage.length > 1) {
			String catName = "";
			if (userMessage.length > 2) {
				catName = message.getContentDisplay().substring(userMessage[0].length() + userMessage[1].length() + 2);
				catName = catName.replace("--owner", "");
				catName = catName.replace("-o", "");
				catName = catName.trim();
			}
			
			switch (userMessage[1].toLowerCase()) {
				case "list":
					if (userMessage.length > 2) {
						channel.sendMessage(getCategoryChannels(catName)).queue();
					} else {
						channel.sendMessage(listCategories()).queue();
					}
					break;
				case "sort":
					if (userMessage.length > 2) {
						if(sortByName(catName)) {
							channel.sendMessage("Die Kanäle in der Kategorie **" + catName + "** wurden alphabetisch sortiert.").queue();
						} else {
							channel.sendMessage("Es wurde keine Kategorie mit dem Namen \"" + catName + "\" gefunden.").queue();
						}
					} else {
						printHelp(channel);
					}
					break;
				case "sync":
					if (userMessage.length > 2) {
						boolean withOwner = false;
						if (usrMsgList.contains("-o") || usrMsgList.contains("--owner")) {
							withOwner = true;
						}
						if(sync(catName, withOwner)) {
							channel.sendMessage("Die Kanäle in der Kategorie **" + catName + "** wurden synchronisiert.").queue();
						} else {
							channel.sendMessage("Es wurde keine Kategorie mit dem Namen \"" + catName + "\" gefunden.").queue();
						}
					} else {
						printHelp(channel);
					}
					break;
				default:
					printHelp(channel);
					break;
			}
		} else {
			printHelp(channel);
		}
	}

	public String getCategoryChannels(String catName) {
		List<net.dv8tion.jda.api.entities.Category> categories = channel.getGuild().getCategoriesByName(catName, false);
		if(categories.size() > 0) {
			StringBuilder categoryChannels = new StringBuilder();
			categoryChannels.append("Die Kategorie **" + catName + "** enthält folgende Kanäle:");
			for(GuildChannel chan : categories.get(0).getChannels()) {
				categoryChannels.append("\n" + chan.getAsMention());
			}
			return categoryChannels.toString();
		} else {
			return "Es wurde keine Kategorie mit dem Namen \"" + catName + "\" gefunden.";
		}
	}
	
	public String listCategories() {
		StringBuilder allCategories = new StringBuilder();
		allCategories.append("**Verfügbare Kategorien:**\n");
		for(net.dv8tion.jda.api.entities.Category cat : channel.getGuild().getCategories()) {
			allCategories.append(cat.getAsMention() + "\n");
		}
		return allCategories.toString();
	}
	
	public boolean sortByName(String catName) {
		List<net.dv8tion.jda.api.entities.Category> categories = channel.getGuild().getCategoriesByName(catName, true);
		if(categories.size() > 0) {
			sort(categories.get(0));
			return true;
		}
		return false;
	}
	
	public static void sort(net.dv8tion.jda.api.entities.Category category) {
		if(category.getTextChannels().size() > 0) {
			category.modifyTextChannelPositions().sortOrder(new ChannelComparator()).queue();
		}
		if(category.getVoiceChannels().size() > 0) {
			category.modifyVoiceChannelPositions().sortOrder(new ChannelComparator()).queue();
		}
	}
	
	private boolean sync(String catName, boolean withOwner) {
		List<net.dv8tion.jda.api.entities.Category> categories = channel.getGuild().getCategoriesByName(catName, true);
		if(categories.size() > 0) {
			sync(categories.get(0), withOwner);
			return true;
		} else {
			return false;
		}
	}
	
	private void sync(net.dv8tion.jda.api.entities.Category category, boolean withOwner) {
		ArrayList<Permission> allow = new ArrayList<Permission>();
		allow.add(Permission.VIEW_CHANNEL);
		allow.add(Permission.MESSAGE_SEND);
		allow.add(Permission.MESSAGE_ATTACH_FILES);
		allow.add(Permission.MESSAGE_EMBED_LINKS);
		
		if(category.getTextChannels().size() > 0 || category.getVoiceChannels().size() > 0) {
			channel.sendMessage("Synchronisiere:").complete();
		}
		
		if(withOwner) { //There is a channel owner
			for(TextChannel textChan : category.getTextChannels()) {
				MessageHistory mh = new MessageHistory(textChan);
				mh.retrievePast(1).complete();
				
				if(mh.getRetrievedHistory().size() > 0) {
					channel.sendMessage(textChan.getAsMention() + " -> Owner: **" + mh.getRetrievedHistory().get(0).getAuthor().getName() + "**").complete();
					textChan.getManager().sync().putMemberPermissionOverride(mh.getRetrievedHistory().get(0).getAuthor().getIdLong(), allow, null).complete();
				} else {
					channel.sendMessage(textChan.getAsMention() + " -> *Kein Owner*").complete();
					textChan.getManager().sync().complete();
				}
			}
		} else {
			if(category.getTextChannels().size() > 0) {
				for(TextChannel textChan : category.getTextChannels()) {
					channel.sendMessage(textChan.getAsMention()).complete();
					textChan.getManager().sync().complete();
				}
			}
		}
		if(category.getVoiceChannels().size() > 0) {
			for(VoiceChannel vchan : category.getVoiceChannels()) {
				channel.sendMessage(vchan.getAsMention()).complete();
				vchan.getManager().sync().complete();
			}
		}
	}
	
}
