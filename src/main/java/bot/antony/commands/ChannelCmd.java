package bot.antony.commands;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;

public class ChannelCmd extends ServerCommand {

	private TextChannel channel;
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ChannelCmd() {
		super();
		this.privileged = true;
		this.name = "channel";
		this.description = "Mit diesem Befehl lassen sich Kanäle administrieren.";
		this.shortDescription = "Befehl zur Administration von Kanälen.";
		this.example = "list abandoned";
		this.cmdParams.put("add [sort, #referenceChannel, @Member] channelName", "Legt einen neuen Kanal mit den Berechtigungen der übergeordneten Kategorie an.");
		this.cmdParams.put("list (abandoned | verlassen) [monate]", "Listet Kanäle auf.");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		this.channel = channel;
		String[] userMessage = message.getContentDisplay().split(" ");
		Guild guild = message.getGuild();
		
		if (userMessage.length > 1) {

			switch (userMessage[1].toLowerCase()) {
			case "add":
				
				if(userMessage.length > 2) {
					String channelName = userMessage[userMessage.length-1];
					TextChannel refChannel = channel;
					if(message.getMentions().getChannels(TextChannel.class).size() > 0) {
						refChannel = message.getMentions().getChannels(TextChannel.class).get(0);
					}
					
					Boolean sort = false;
					if(userMessage[2].equalsIgnoreCase("sort")) {
						sort = true;
					}
					
					TextChannel newChan = addChannel(channelName, refChannel.getParentCategory(), message.getMentions().getMembers(), message.getMember(), sort);
					
					//feedback
					channel.sendMessage(newChan.getAsMention() + " wurde angelegt.").complete();
					
				} else {
					printHelp(channel);
				}
				
				break;
			case "list":
				
				if(userMessage.length > 2) {
					switch (userMessage[2].toLowerCase()) {
					case "abandoned":
					case "verlassen":
						channel.sendMessage("Ich prüfe alle Kanäle...").complete();
						Message response = getLatestBotMessage();
						
						List<TextChannel> channelsToReport = new ArrayList<TextChannel>();
						int months = 6;
						if((userMessage.length > 3)) {
							months = getAbandonedMonths(userMessage[3]);
						}
						List<Category> categories = guild.getCategories();
						
						//for category...
						int channelCount = 0;
						int percentage = 0;
						int channelSize = channel.getGuild().getChannels(true).size() - channel.getGuild().getCategories().size();
						for(Category category : categories) {
							//if category is not whitelisted...
							if(!catIsWhitelisted(category)) {
								channelsToReport.addAll(getChannelsToReport(category, months));
							}
							channelCount += category.getChannels().size();
							percentage = (int) (channelCount/(double)(channelSize)*100);
							response.editMessage("Ich prüfe alle Kanäle: **" + percentage + "%**").complete();
						}
						response.editMessage("Ich prüfe alle Kanäle: **100%**").complete();
						printChannelsToReport(channelsToReport, months);
						
						break;
					default:
						printHelp(channel);
						break;
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
	
	public static TextChannel addChannel(String chanName, Category cat, List<Member> members, Member mod, Boolean sort) {
		StringBuilder chanTopic = new StringBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		LocalDateTime date = LocalDateTime.now();
		
		//Create channel with category permissions
		TextChannel newChan = cat.createTextChannel(chanName).syncPermissionOverrides().complete();
		
		//If there is a channel owner it will be mentioned in the topic
		if(members.size() > 0) {
			chanTopic.append("Kanal von: ");
		}
		int counter = 1;
		for(Member member : members) {
			//Add read/write permission
			ArrayList<Permission> allow = new ArrayList<Permission>();
			//allow.add(Permission.MESSAGE_READ);
			allow.add(Permission.VIEW_CHANNEL);
			allow.add(Permission.MESSAGE_SEND);
			allow.add(Permission.MESSAGE_ATTACH_FILES);
			allow.add(Permission.MESSAGE_EMBED_LINKS);
			newChan.getManager().putMemberPermissionOverride(member.getIdLong(), allow, null).complete();
			
			//Add name to channel topic
			chanTopic.append(member.getEffectiveName());
			if(counter < members.size()) {
				chanTopic.append(", ");
			}
			counter++;
		}
		
		//Add creation date to channel topic
		chanTopic.append("\nErstellt: " + date.format(formatter) + " Uhr (von " + mod.getEffectiveName() + ")");
		
		//Set channel topic
		newChan.getManager().setTopic(chanTopic.toString()).complete();
		
		//Send notification to channel owner if necessary
		if(members.size() > 0) {
			StringBuilder notifyMsg = new StringBuilder();
			
			counter = 1;
			for(Member member : members) {
				notifyMsg.append(member.getAsMention());
				if(counter < members.size()) {
					notifyMsg.append(" ");
				}
				counter++;
			}
			notifyMsg.append(" hier ist");
			if(members.size() == 1) {
				notifyMsg.append(" dein");
			} else {
				notifyMsg.append(" euer");
			}
			notifyMsg.append(" neuer Kanal. Viel Spaß beim Schreiben 🙂");
			
			//TODO catch exception SEVERE: RestAction queue returned failure: [ErrorResponseException] 10008: Unknown Message
			newChan.sendMessage(notifyMsg.toString()).complete().delete().queueAfter(10, TimeUnit.MINUTES);
		}
		
		//sort if necessary
		if(sort) {
			bot.antony.commands.CategoryCmd.sort(cat);
		}
		
		return newChan;
	}
	
	private int getAbandonedMonths(String txtSnippet) {
		int months = 6;
		try {
			months = Integer.parseInt(txtSnippet);
		}
		catch (NumberFormatException e) {
			//months = 6;
		}
		return months;
	}
	
	private boolean catIsWhitelisted(Category category) {
		List<String> whitelist = new ArrayList<String>();
		whitelist.add("archiv");
		whitelist.add("geschlossen");
		for(String wlcat: whitelist) {
			if(category.getName().toLowerCase().contains(wlcat.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	private List<TextChannel> getChannelsToReport(Category category, int months){
		List<TextChannel> channelsToReport = new ArrayList<TextChannel>();
		List<TextChannel> textChannels = category.getTextChannels();
		Antony.getLogger().info("Searching in Category: " + category);
		//for text channel...
		for(TextChannel textChannel: textChannels) {
			if(channel.getGuild().getMemberById(channel.getJDA().getSelfUser().getId()).hasPermission(textChannel, Permission.VIEW_CHANNEL)) {
				Antony.getLogger().debug("Get message history of channel: " + textChannel.getName());
				MessageHistory history = new MessageHistory(textChannel);
				List<Message> messageList = history.retrievePast(1).complete();
				//Check if channel is empty and older than 2 Weeks
				if(messageList.isEmpty() && textChannel.getTimeCreated().plusWeeks(2).isBefore(OffsetDateTime.now())) {
					channelsToReport.add(textChannel);
					Antony.getLogger().debug("Added Channel to Report: " + textChannel.getName());
				} else if(!messageList.isEmpty() &&  messageList.get(0).getTimeCreated().plusMonths(months).isBefore(OffsetDateTime.now())) {
					channelsToReport.add(textChannel);
				}
			}
		}
		return channelsToReport;
	}
	
	private void printChannelsToReport(List<TextChannel> channelsToReport, int months) {
		if(!channelsToReport.isEmpty()) {
			StringBuilder output = new StringBuilder();
			output.append("Folgende Kanäle haben keine Posts, die jünger als " + months + " Monate sind oder sie sind leer und älter als 2 Wochen: \n");

			int counter = 1;
			for(TextChannel textChannel : channelsToReport) {
				if((output.length() + textChannel.getAsMention().length()) >= 1998) {
					channel.sendMessage(output.toString()).queue();
					output.delete(0, output.length());
				}
				output.append(textChannel.getAsMention());
				if(counter < channelsToReport.size()) {
					output.append(", ");
					counter++;
				}
			}
			channel.sendMessage(output.toString()).queue();
		} else {
			channel.sendMessage("Ich konnte keine Kanäle finden, deren letzter Post älter als " + months + " Monate ist.").queue();
		}
	}
	
	private Message getLatestBotMessage() {
		MessageHistory history = new MessageHistory(channel);
		List<Message> messageList = history.retrievePast(50).complete();
		for(Message msg : messageList) {
			if(msg.getAuthor() == channel.getJDA().getSelfUser()) {
				return msg;
			}
		}
		return null;
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------

}