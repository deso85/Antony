package bot.antony.events;

import java.awt.Color;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import bot.antony.Antony;
import bot.antony.guild.GuildData;
import bot.antony.guild.channel.ChannelData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class WatchlistNotification extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

		String message = event.getMessage().getContentDisplay();

		// check which channel ...
		if (event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot()) {
			List<String> watchlist = Antony.getWatchlistController().getWatchlist();
			List<String> whitelist = Antony.getWatchlistController().getWhitelist();
			String modifiedMessage = message;
			final Guild guild = event.getGuild();
			final TextChannel channel = event.getTextChannel();
			GuildData guildData = new GuildData(guild);
			ChannelData channelData = new ChannelData(channel);
			
			/*
			for(String string: watchlist) {
				if(message.toLowerCase().contains(string)) {
					StringBuilder sb = new StringBuilder();
					sb.append("Wort erkannt: " + string + "\n");
					sb.append("Kanal: " + event.getTextChannel().getAsMention() + "\n");
					sb.append("Author: " + event.getAuthor().getAsTag() + " (ID: " + event.getAuthor().getId() + ")\n");
					sb.append("Nachricht: https://discord.com/channels/" + guildData.getId() + "/" + channel.getId() + "/" + event.getMessageId());
					guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(sb.toString()).queue();
					guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(message).queue();
				}
			}
			*/
			
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
							//.setDescription(message.replace(string, "__" + string + "__"))
							.addField("#" + event.getMessage().getChannel().getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + guildData.getId() + "/" + channel.getId() + "/" + event.getMessageId() + ")**", false)
							.setFooter(event.getMessage().getTimeCreated().format(formatter));
					
					guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(sb.toString()).queue();
					guild.getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(eb.build()).queue();
				}
			}
			
		}
	}
}
