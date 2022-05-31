package bot.antony.events;

import java.util.ArrayList;

import bot.antony.Antony;
import bot.antony.commands.notification.NotificationController;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMemberLeave extends ListenerAdapter {

	Guild guild;
	Member member;
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		NotificationController nc = Antony.getNotificationController();
		guild = event.getGuild();
		member = event.getMember();
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
	    logMessage.append("quitted Discord server [" + guildData.toString() + "]. ");
	    
	    //Remove user from notification lists if
	    nc.removeUserFromAllListsOfGuild(guildData, userData);
	    nc.persistData();
	    
	    logMessage.append("Removed user from all notification lists.");
	    Antony.getLogger().info(logMessage.toString());
	    
	    //update usercount
	    Antony.setUsercount(Antony.getUsercount()-1);
	    event.getJDA().getPresence().setActivity(Activity.listening(Antony.getCmdPrefix() + "antony | " + Antony.getUsercount() + " User | " + event.getJDA().getGuilds().size() + " Server"));
	    
	    //Send exit message about user
	    if(Antony.getGuildController().getExitChannel(guild) != null) {
	    	Antony.getGuildController().getExitChannel(guild).sendMessage(getRandomExitText()).queue();
	    }
	}
	
	public String getRandomExitText() {
		ArrayList<String> messages = new ArrayList<String>();
		
		messages.add("**" + member.getUser().getAsTag() + "** hat uns grade verlassen. 🙁");
		
		return messages.get((int) (System.currentTimeMillis() % messages.size()));
	}
}
