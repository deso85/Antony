package bot.antony.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;

public class Archive implements ServerCommand {
	
	TextChannel channel;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		this.channel = channel;

		String[] userMessage = message.getContentDisplay().split(" ");
		if (userMessage.length > 1 && message.getMentions().getChannels(TextChannel.class).size() > 0) {
			
			StringBuilder returnMessage = new StringBuilder();
			TextChannel archiveChan = message.getMentions().getChannels(TextChannel.class).get(0);
			List<Message> msgHistory;
			DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
			DateTimeFormatter textFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String archivePath = Antony.getGuildController().getStoragePath(message.getGuild()) + File.separator + "archive" + File.separator;
			String filePath = archivePath + "#" + archiveChan.getName() + "_" + now.format(fileFormatter) + ".html";
			int msgCount = 0;
			
			if(userMessage.length > 2) {
				try {
					msgCount = Integer.parseInt(userMessage[2]);
				} catch (NumberFormatException e) {
					printHelp();
				}
			}
			
			if(msgCount == 0) {
				msgHistory = getMessageHistory(archiveChan);
			} else {
				msgHistory = getMessageHistory(archiveChan, msgCount);
			}
			
			if(msgHistory.size() > 0) {
				try {
					
					File directory = new File(archivePath);
				    if (! directory.exists()){
				        directory.mkdirs();
				    }
					
				    
				    InputStream is = Antony.class.getResourceAsStream("/archive_template.html");
				    InputStreamReader isr = new InputStreamReader(is);
				    BufferedReader br = new BufferedReader(isr);
				    
				    StringBuilder htmlCode = new StringBuilder();
				    String line;
				    int lineCount = 0;
				    while ((line = br.readLine()) != null) 
				    {
				    	if(lineCount != 0) {
				    		htmlCode.append("\n");
				    	}
				    	htmlCode.append(line);
				    	lineCount++;
				    }
				    
				    br.close();
				    isr.close();
				    is.close();
				    
				    net.dv8tion.jda.api.entities.User archiveUser = Lists.reverse(msgHistory).get(0).getAuthor();
				    htmlCode = replaceAll(htmlCode, "REPLACESERVERNAME", archiveChan.getGuild().getName());
				    htmlCode = replaceAll(htmlCode, "REPLACECATEGORY", archiveChan.getParentCategory().getName());
				    htmlCode = replaceAll(htmlCode, "REPLACECHANNELNAME", archiveChan.getName());
				    htmlCode = replaceAll(htmlCode, "REPLACEUSERTAG", archiveUser.getAsTag());
				    
				    StringBuilder htmlContent = new StringBuilder(); 
				    for(Message msg : Lists.reverse(msgHistory)) {
				    	net.dv8tion.jda.api.entities.User msgAuthor = msg.getAuthor();
				    	
				    	htmlContent.append("<div class=\"message\">");
				    	
					    	//Avatar
					    	htmlContent.append("<div class=\"icon\">");
					    	htmlContent.append("<img class=\"avatar\" src=\"" + msgAuthor.getAvatarUrl() + "\">");
					    	htmlContent.append("</div>");
					    	
					    	//Body
					    	htmlContent.append("<div class=\"body\">");
						    	htmlContent.append("<div class=\"username\">" + msgAuthor.getName() + "</div>");
						    	htmlContent.append("<div class=\"date\">" + msg.getTimeCreated().atZoneSameInstant(ZoneId.systemDefault()).format(textFormatter) + "</div>");
						    	htmlContent.append("<div class=\"content\">" + msg.getContentDisplay());
						    	
						    	for(Attachment att : msg.getAttachments()) {
						    		ArrayList<String> list = new ArrayList<String>(Arrays.asList("jpeg", "jpg", "png", "svg", "webp", "gif", "tiff", "bmp", "ico", "apng", "avif"));
						    		
						    		if(endsWith(att.getUrl(), list)) {
						    			htmlContent.append("\n<img src=\"" + att.getUrl() + "\"/>\n");
						    		} else {
						    			htmlContent.append("\n<a href=\"" + att.getUrl() + "\">" + att.getUrl() + "</a>\n");
						    		}
								}
						    	
						    	htmlContent.append("</div>");
					    	htmlContent.append("</div>");
				    	
				    	htmlContent.append("</div>");
				    }
				    
				    htmlCode = replaceAll(htmlCode, "REPLACECONTENT", htmlContent.toString());
				    
				    FileOutputStream fos = new FileOutputStream(filePath);
				    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
				    BufferedWriter writer = new BufferedWriter(osw);
				    writer.append(htmlCode.toString());
				    writer.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				channel.sendMessage("Es wurden " + msgHistory.size() + " Nachrichten aus dem Kanal **#" + archiveChan.getName() + "** archiviert.").complete();
				File archiveAttachment = new File(filePath);
				channel.sendFile(archiveAttachment).complete();
			}
			
			if(returnMessage.length() > 0) {
				channel.sendMessage(returnMessage.toString()).queue();
			}
				

		} else {
			printHelp();
		}
	}
	
	private void printHelp() {
		channel.sendMessage("Benutzung: " + Antony.getCmdPrefix() + "archive #TextChannel [*Anzahl Nachrichten*]").queue();
	}
	
	private List<Message> getMessageHistory(TextChannel channel) {
		MessageHistory history = channel.getHistory();
		
		int historySize = 0;
		boolean iterate = true;
		while(iterate) {
			//history.getRetrievedHistory().size() % 100 == 0
			history.retrievePast(100).complete();
			if(historySize != history.getRetrievedHistory().size()) {
				historySize = history.getRetrievedHistory().size();
			} else {
				iterate = false;
			}
		}
		return history.getRetrievedHistory();
	}
	
	private List<Message> getMessageHistory(TextChannel channel, int messageCount) {
		MessageHistory history = channel.getHistory();
		
		while(messageCount > 0) {
			if(messageCount>100) {
				history.retrievePast(100).complete();
				messageCount = messageCount-100;
			} else {
				history.retrievePast(messageCount).complete();
				messageCount=0;
			}
		}
		
		return history.getRetrievedHistory();
	}
	
	private StringBuilder replaceAll(StringBuilder sb, String find, String replace) {
	    return new StringBuilder(Pattern.compile(find).matcher(sb).replaceAll(replace));
	}
	
	private boolean endsWith(String toCheck, ArrayList<String> list) {
		for(String string : list) {
			if(toCheck.toLowerCase().endsWith(string.toLowerCase())) {
				return true;
			}
		}
		
		return false;
	}
}
