package bot.antony.events;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.events.softban.UserDataSB;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageUpdate extends ListenerAdapter {

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		
		// check which channel ...
		if ((event.isFromType(ChannelType.TEXT) || event.isFromThread()) &&
				!event.getAuthor().equals(event.getJDA().getSelfUser())) {
			
			if(event.getMember() != null && !event.getMember().getOnlineStatus().equals(OnlineStatus.ONLINE)) {
				UserController usrCntrl = Antony.getUserController();
				UserData user = usrCntrl.loadUserData(event.getMember());
				user.setLastOnline(System.currentTimeMillis());
				usrCntrl.saveUserData(user, event.getGuild());
			}
			
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
				}
			}
			
		}
	}
}
