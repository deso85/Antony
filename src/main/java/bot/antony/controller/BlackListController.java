package bot.antony.controller;

import java.awt.Color;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import bot.antony.Antony;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class BlackListController extends ListController {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public BlackListController() {
		super();
		fileName = "antony.blacklist.json";
		initData();
	}
	
	public boolean checkBlacklistedContent(Message message) {
		Guild guild = message.getGuild();
		TextChannel channel = message.getTextChannel();
		TextChannel rspChannel = Antony.getGuildController().getLogChannel(guild);
		String msg = message.getContentDisplay();
		String wlmsg = Antony.getWhitelistController().getCleanedMessage(msg);
		
		boolean blacklisted = false;
		for(String string : getList()) {
			if(wlmsg.toLowerCase().contains(string)) {
				StringBuilder sb = new StringBuilder();
				sb.append("🚨 Auffälliges Wort erkannt: **" + string + "**");
				sb.append("\nDie Nachricht wurde sofort gelöscht!");
				
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());
				EmbedBuilder eb = new EmbedBuilder()
						.setColor(Color.red)
						.setAuthor(message.getAuthor().getAsTag() + " | ID: " + message.getAuthor().getId(), null, message.getAuthor().getAvatarUrl())
						.setDescription(message.getContentDisplay())
						.addField("#" + channel.getName(), "**[Hier klicken, um zum Kanal zu kommen.](https://discord.com/channels/" + guild.getId() + "/" + channel.getId() + "/" + ")**", false)
						.setFooter(message.getTimeCreated().format(formatter));
				
				if(rspChannel != null) {
					rspChannel.sendMessage(sb.toString()).complete();
					rspChannel.sendMessageEmbeds(eb.build()).complete();
				
					if(message.getAttachments().size() > 0) {
						rspChannel.sendMessage("Vorsicht! Folgende Attachments wurden gepostet:").complete();
						for(Attachment attachment : message.getAttachments()) {
							rspChannel.sendMessage(attachment.getUrl()).complete();
						}
					}
				}
				
				UserData usrData = new UserData(message.getAuthor());
				Antony.getLogger().info("🚨 Message from " + usrData.toString() + " deleted because of blacklisted word: " + string);
				
				message.delete().complete();
				blacklisted = true;
			}
		}
		return blacklisted;
		
	}
}
