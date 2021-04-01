package bot.antony.events;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OfferListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContentDisplay();
		User user = event.getAuthor();
		String userString = user.getAsTag() + " (ID: " + user.getId() + ")";
				
		//check which channel ...
		if(event.isFromType(ChannelType.TEXT)) {
			final TextChannel channel = event.getTextChannel();
			Guild guild = event.getGuild();
			
			//TODO: Use Variable
			long offerChannelId = 778960516461625347L;		//is test ID
			long offerLogChannelId = 778960515631415324L;	//is test ID
			
			if(Antony.isProdStage()) {
				offerChannelId = 543512785171251201L;
				offerLogChannelId = 726444104627519549L;
			}
			
			//Is a new offer
			if(channel.getIdLong() == offerChannelId) {
				//SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				SimpleDateFormat sdformatter = new SimpleDateFormat("dd.MM.yyyy");
				DateTimeFormatter dtformatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
				Date now = new Date(System.currentTimeMillis());
				
				//TODO: may the user add a new offer?
				
				if(guild.getTextChannelById(offerLogChannelId).hasLatestMessage()) {
					Message latestMessage = guild.getTextChannelById(offerLogChannelId).retrieveMessageById(guild.getTextChannelById(offerLogChannelId).getLatestMessageId()).complete();
					//Shall the last post be updated / replaced?
					if(latestMessage.getTimeCreated().atZoneSameInstant(ZoneId.systemDefault()).format(dtformatter).equals(sdformatter.format(now))) {
						if(!latestMessage.getContentDisplay().contains(userString)) {
							guild.getTextChannelById(offerLogChannelId).sendMessage(latestMessage.getContentDisplay() + ", " + userString).queue();
							latestMessage.delete().queue();
						}
					} else {
						guild.getTextChannelById(offerLogChannelId).sendMessage(sdformatter.format(now) + ": " + userString).queue();
					}
				} else {
					guild.getTextChannelById(offerLogChannelId).sendMessage(sdformatter.format(now) + ": " + userString).queue();
				}
			}
		}
	}
}
