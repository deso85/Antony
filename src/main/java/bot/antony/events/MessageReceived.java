package bot.antony.events;

import bot.antony.Antony;
import bot.antony.events.softban.UserDataSB;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReceived extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		// check which channel ...
		if (event.isFromType(ChannelType.TEXT) &&
				!event.getAuthor().equals(event.getJDA().getSelfUser())) {
			
			//Message parts on blacklist?
			if(Antony.getGuildController().memberIsMod(event.getMember()) ||
					!Antony.getBlacklistController().checkBlacklistedContent(event.getMessage())) {
				
				//User is softbanned?
				UserDataSB user = new UserDataSB(event.getAuthor().getId(), event.getAuthor().getName());
				if(Antony.getSoftbanController().getBannedUser().contains(user)) {
					event.getMessage().delete().complete();
					Antony.getLogger().info("Message in channel #" + event.getChannel().getName() + " sent by user \"" + event.getAuthor().getName() + "\" (UID: " + event.getAuthor().getId() + ") "
						+ "deleted because this user is softbanned.");
				} else {
					//Message parts on watchlist?
					Antony.getWatchlistController().checkWatchlistedContent(event.getMessage());
					
					//Notification for user needed?
					
					//Message is a command?
				}
			}
		}
	}
}
