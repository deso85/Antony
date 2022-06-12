package bot.antony.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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

public class Archive extends ServerCommand {
		
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public Archive() {
		super();
		this.privileged = true;
		this.name = "archive";
		this.description = "Mit diesem Befehl lassen sich Kanalinhalte archivieren und als HTML-Datei herunterladen.";
		this.shortDescription = "Befehl zur Archivierung von Kanalinhalten.";
		this.example = "#channel 50";
		this.cmdParams.put("#channel (MessageCount)", "Archiviert die Inhalte von #channel. MessageCount ist optional und limitiert die archivierten Nachrichten.");
	}
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		if (userMessage.length > 1 && message.getMentions().getChannels(TextChannel.class).size() > 0) {
			TextChannel archiveChan = message.getMentions().getChannels(TextChannel.class).get(0);
			DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
			String archivePath = Antony.getGuildController().getStoragePath(channel.getGuild()) + File.separator + "archive" + File.separator;
			String filePath = archivePath + "#" + archiveChan.getName() + "_" + LocalDateTime.now().format(fileFormatter) + ".html";
			
			int msgCount = 0;
			if(userMessage.length > 2) {
				try {
					msgCount = Integer.parseInt(userMessage[2]);
				} catch (NumberFormatException e) {
					printHelp(channel);
				}
			}
			List<Message> msgHistory = getMessageHistory(archiveChan, msgCount);
			
			if(msgHistory.size() > 0) {
				StringBuilder htmlOutput = replaceHtmlContent(getHtmlCodeTemplate(archivePath), getHtmlContentFromMsgHistory(msgHistory), archiveChan);
				storeArchiveAsHTML(htmlOutput.toString(), filePath);
				channel.sendMessage("Es wurden " + msgHistory.size() + " Nachrichten aus dem Kanal **#" + archiveChan.getName() + "** archiviert.").complete();
				File archiveAttachment = new File(filePath);
				channel.sendFile(archiveAttachment).complete();
			}
		} else {
			printHelp(channel);
		}
	}
	
	private StringBuilder getHtmlContentFromMsgHistory(List<Message> msgHistory) {
		DateTimeFormatter textFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
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
		return htmlContent;
	}
	
	private void storeArchiveAsHTML(String htmlCode, String filePath) {
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		    BufferedWriter writer = new BufferedWriter(osw);
		    writer.append(htmlCode);
		    writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private StringBuilder getHtmlCodeTemplate(String archivePath) {
		StringBuilder htmlCode = new StringBuilder();
	    try {
			File directory = new File(archivePath);
		    if (! directory.exists()){
		        directory.mkdirs();
		    }
		    
		    InputStream is = Antony.class.getResourceAsStream("/archive_template.html");
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);

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
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return htmlCode;
	}
	
	private List<Message> getMessageHistory(TextChannel channel, int limit) {
		MessageHistory history = channel.getHistory();
			if(limit > 0) {
				while(limit > 0) {
					if(limit > 100) {
						history.retrievePast(100).complete();
						limit = limit-100;
					} else {
						history.retrievePast(limit).complete();
						limit = 0;
					}
				}
			} else {
				int historySize;
				do {
					historySize = history.getRetrievedHistory().size();
					history.retrievePast(100).complete();
				} while(historySize != history.getRetrievedHistory().size());
			}
		return history.getRetrievedHistory();
	}
	
	private StringBuilder replaceHtmlContent(StringBuilder htmlCode, StringBuilder htmlContent, TextChannel archiveChan) {
		htmlCode = replaceAll(htmlCode, "REPLACESERVERNAME", archiveChan.getGuild().getName());
		htmlCode = replaceAll(htmlCode, "REPLACECATEGORY", archiveChan.getParentCategory().getName());
		htmlCode = replaceAll(htmlCode, "REPLACECHANNELNAME", archiveChan.getName());
		htmlCode = replaceAll(htmlCode, "REPLACECONTENT", htmlContent.toString());
		return htmlCode;
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
