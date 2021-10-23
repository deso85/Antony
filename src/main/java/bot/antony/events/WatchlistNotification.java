package bot.antony.events;

import java.awt.Color;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import bot.antony.Antony;
import bot.antony.guild.GuildData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class WatchlistNotification extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		// check which channel ...
		if (event.isFromType(ChannelType.TEXT) &&
				!event.getAuthor().isBot() &&
				!Utils.isLogChannel(event.getTextChannel())) {
			
			final Guild guild = event.getGuild();
			final TextChannel channel = event.getTextChannel();
			TextChannel responseChannel = Antony.getGuildController().getLogChannel(guild);
			String message = event.getMessage().getContentDisplay();
			
			List<String> watchlist = Antony.getWatchlistController().getList();
			List<String> whitelist = Antony.getWhitelistController().getList();
			String modifiedMessage = message;
			
			GuildData guildData = new GuildData(guild);
			
			for(String string: whitelist) {
				modifiedMessage = modifiedMessage.replaceAll("(?i)" + Pattern.quote(string), "");
			}
			for(String string: watchlist) {
				if(modifiedMessage.toLowerCase().contains(string)) {
					StringBuilder sb = new StringBuilder();
					sb.append(":exclamation:Auffälliges Wort erkannt: **" + string + "**");
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());
					EmbedBuilder eb = new EmbedBuilder()
							.setColor(Color.orange)
							.setAuthor(event.getAuthor().getAsTag() + " | ID: " + event.getAuthor().getId(), null, event.getAuthor().getAvatarUrl())
							.setDescription(message.replaceAll("(?i)" + Pattern.quote(string), "__" + string + "__"))
							.addField("#" + event.getMessage().getChannel().getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + guildData.getId() + "/" + channel.getId() + "/" + event.getMessageId() + ")**", false)
							.setFooter(event.getMessage().getTimeCreated().format(formatter));
					
					if(responseChannel != null) {
						responseChannel.sendMessage(sb.toString()).complete();
						responseChannel.sendMessageEmbeds(eb.build()).complete();
					}
				}
			}
			
		}
	}
}
