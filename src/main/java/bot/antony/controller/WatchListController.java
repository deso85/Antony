package bot.antony.controller;

import java.awt.Color;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import bot.antony.Antony;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class WatchListController extends ListController {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public WatchListController() {
		super();
		fileName = "antony.watchlist.json";
		initData();
	}
	
	public boolean checkWatchlistedContent(Message message) {
		Guild guild = message.getGuild();
		TextChannel rspChannel = Antony.getGuildController().getLogChannel(guild);
		User user = message.getAuthor();
		String msg = message.getContentDisplay();
		String wlmsg = Antony.getWhitelistController().getCleanedMessage(msg);
		
		boolean watchlisted = false;
		for(String string : getList()) {
			if(wlmsg.toLowerCase().contains(string)) {
				StringBuilder sb = new StringBuilder();
				sb.append("❗ Auffälliges Wort erkannt: **" + string + "**");
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());
				EmbedBuilder eb = new EmbedBuilder()
						.setColor(Color.orange)
						.setAuthor(user.getAsTag() + " | ID: " + user.getId(), null, user.getAvatarUrl())
						.setDescription(msg.replaceAll("(?i)" + Pattern.quote(string), "__" + string + "__"))
						.addField("#" + message.getChannel().getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + guild.getId() + "/" + message.getChannel().getId() + "/" + message.getId() + ")**", false)
						.setFooter(message.getTimeCreated().format(formatter));
				
				if(rspChannel != null) {
					rspChannel.sendMessage(sb.toString()).complete();
					rspChannel.sendMessageEmbeds(eb.build()).complete();
				}
				watchlisted = true;
			}
		}
		return watchlisted;
	}
}
