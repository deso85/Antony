package bot.antony.events;

import java.awt.Color;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import bot.antony.Antony;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BlacklistNotification extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		final Guild guild = event.getGuild();
		final TextChannel channel = event.getTextChannel();
		String message = event.getMessage().getContentDisplay();

		// check which channel ...
		if (event.isFromType(ChannelType.TEXT) &&
				!event.getAuthor().isBot() &&
				!Utils.isLogChannel(channel)) {
			List<String> blacklist = Antony.getBlacklistController().getList();
			List<String> whitelist = Antony.getWhitelistController().getList();
			String modifiedMessage = message;
			
			
			for(String string: whitelist) {
				modifiedMessage = modifiedMessage.replaceAll("(?i)" + Pattern.quote(string), "");
			}
			for(String string: blacklist) {
				if(modifiedMessage.toLowerCase().contains(string)) {
					StringBuilder sb = new StringBuilder();
					sb.append("🚨 Auffälliges Wort erkannt: **" + string + "**");
					sb.append("\nDie Nachricht wurde sofort gelöscht!");
					
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());
					EmbedBuilder eb = new EmbedBuilder()
							.setColor(Color.red)
							.setAuthor(event.getAuthor().getAsTag() + " | ID: " + event.getAuthor().getId(), null, event.getAuthor().getAvatarUrl())
							.setDescription(message)
							.addField("#" + event.getMessage().getChannel().getName(), "**[Hier klicken, um zum Kanal zu kommen.](https://discord.com/channels/" + guild.getId() + "/" + channel.getId() + "/" + ")**", false)
							.setFooter(event.getMessage().getTimeCreated().format(formatter));
					
					Utils.getLogChannel(guild).sendMessage(sb.toString()).complete();
					Utils.getLogChannel(guild).sendMessageEmbeds(eb.build()).complete();
					
					if(event.getMessage().getAttachments().size() > 0) {
						Utils.getLogChannel(guild).sendMessage("Vorsicht! Folgende Attachments wurden gepostet:").complete();
						for(Attachment attachment : event.getMessage().getAttachments()) {
							Utils.getLogChannel(guild).sendMessage(attachment.getUrl()).complete();
						}
					}
					
					event.getMessage().delete().complete();
				}
			}
		}
	}
}
