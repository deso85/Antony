package bot.antony.events;

import bot.antony.Antony;
import bot.antony.notifications.NotificationController;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMemberLeave extends ListenerAdapter {

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		final Guild guild = event.getGuild();
	    final User user = event.getUser();
	    final SelfUser selfUser = event.getJDA().getSelfUser();
	    NotificationController nc = Antony.getNotificationController();
	    
	    // If we are leaving we need to ignore this as we cannot send messages to any channels
	    // when this event is fired
	    if (user.equals(selfUser)) {
	        return;
	    }
	    
	    StringBuilder logMessage = new StringBuilder();
	    logMessage.append("User \"" + user.getName() + "\" (" + user.getId() + ") ");
	    logMessage.append("quitted Discord server \"" + guild.getName() + "\" (" + guild.getId() + "). ");
	    
	    //Remove user from notification lists if
	    nc.removeFromAllListsOfGuild(guild.getId(), user.getId());
	    nc.persistData();
	    
	    logMessage.append("Removed that user from all notification lists.");
	    Antony.getLogger().info(logMessage.toString());
	    
	}
}
