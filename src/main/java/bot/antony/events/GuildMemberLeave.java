package bot.antony.events;

import bot.antony.Antony;
import bot.antony.commands.notification.NotificationController;
import bot.antony.guild.GuildData;
import bot.antony.guild.user.UserData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMemberLeave extends ListenerAdapter {

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		NotificationController nc = Antony.getNotificationController();
		final Guild guild = event.getGuild();
	    final User user = event.getUser();
	    final SelfUser selfUser = event.getJDA().getSelfUser();
	    GuildData guildData = new GuildData(guild);
	    UserData userData = new UserData(user);
	    
	    // If we are leaving we need to ignore this as we cannot send messages to any channels
	    // when this event is fired
	    if (user.equals(selfUser)) {
	        return;
	    }
	    

	    StringBuilder logMessage = new StringBuilder();
	    logMessage.append("User [" + userData.toString() + "] ");
	    logMessage.append("quitted Discord server " + guildData.toString() + "]. ");
	    
	    //Remove user from notification lists if
	    nc.removeUserFromAllListsOfGuild(guildData, userData);
	    nc.persistData();
	    
	    logMessage.append("Removed user from all notification lists.");
	    Antony.getLogger().info(logMessage.toString());
	    
	}
}
