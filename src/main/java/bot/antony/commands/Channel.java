package bot.antony.commands;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Channel implements ServerCommand {

	private TextChannel channel;

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		setChannel(channel);

		List<String> allowedRoles = new ArrayList<String>();
		
		//Roles which may use the command
		allowedRoles.add("Admin");
		allowedRoles.add("Soldat");
		
		boolean mayUse = false;
		for(Role role: m.getRoles()) {
			if(allowedRoles.contains(role.getName())) {
				mayUse = true;
			}
		}
		
		if(mayUse) {
			String[] userMessage = message.getContentDisplay().split(" ");
			Guild guild = message.getGuild();
			
			if (userMessage.length > 1) {

				switch (userMessage[1].toLowerCase()) {
				case "list":
					
					if(userMessage.length > 2) {
						switch (userMessage[2].toLowerCase()) {
						case "abandoned":
						case "verlassen":
							List<TextChannel> channelsToReport = new ArrayList<TextChannel>();
							int months = 6;
							if((userMessage.length > 3)) {
								try {
									months = Integer.parseInt(userMessage[3]);
								}
								catch (NumberFormatException e) {
									//months = 6;
								}
							}
							
							List<Category> categories = guild.getCategories();
							List<String> whitelist = new ArrayList<String>();
							whitelist.add("archiv");
							whitelist.add("geschlossen");
							
							//for category...
							for(Category category: categories) {
								boolean whitelisted = false;
								for(String wlcat: whitelist) {
									if(category.getName().toLowerCase().contains(wlcat.toLowerCase())) {
										whitelisted = true;
									}
								}
								
								//if category is not whitelisted...
								if(!whitelisted) {
									List<TextChannel> textChannels = category.getTextChannels();
									Antony.getLogger().info("Searching in Category: " + category);
									//for text channel...
									for(TextChannel textChannel: textChannels) {
										if(guild.getMemberById(message.getJDA().getSelfUser().getId()).hasPermission(textChannel, Permission.VIEW_CHANNEL)) {
											Antony.getLogger().debug("Get message history of channel: " + textChannel.getName());
											MessageHistory history = new MessageHistory(textChannel);
											List<Message> messageList = history.retrievePast(1).complete();
											//Check if channel is empty and older than 2 Weeks
											if((messageList.size() < 1) && (textChannel.getTimeCreated().plusWeeks(2).isBefore(OffsetDateTime.now()))) {
												channelsToReport.add(textChannel);
												Antony.getLogger().debug("Added Channel to Report: " + textChannel.getName());
											}
											for (Message msg : messageList) {
												//System.out.println(msg.getTimeCreated().toString() + msg.getContentDisplay());
												if(msg.getTimeCreated().plusMonths(6).isBefore(OffsetDateTime.now())) {
													channelsToReport.add(textChannel);
												}
							                }
										}
									}
								}
							}
							
							if(channelsToReport.size() > 0) {
								StringBuilder output = new StringBuilder();
								output.append("Folgende Kanäle haben keine Posts, die jünger als " + months + " Monate sind oder sie sind leer und älter als 2 Wochen: \n");

								int counter = 1;
								for(TextChannel textChannel: channelsToReport) {
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
							break;
						default:
							printHelp();
							break;
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
	}


	private void printHelp() {
		//TODO: Help ausformulieren
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "channel (list (abandoned|verlassen)) [monate]").queue();
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