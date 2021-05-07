package bot.antony.events;

import bot.antony.Antony;
import bot.antony.guild.GuildData;
import bot.antony.guild.user.UserData;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMemberJoin extends ListenerAdapter {

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		final Guild guild = event.getGuild();
	    final User user = event.getUser();
	    GuildData guildData = new GuildData(guild);
	    UserData userData = new UserData(user);
	    
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("User [" + userData.toString() + "] ");
	    logMessage.append("joined Discord server [" + guildData.toString() + "].");
		
		//update usercount
	    Antony.setUsercount(Antony.getUsercount()+1);
	    event.getJDA().getPresence().setActivity(Activity.listening(Antony.getCmdPrefix() + "antony | " + Antony.getUsercount() + " User | " + event.getJDA().getGuilds().size() + " Server"));
	    
	    Antony.getLogger().info(logMessage.toString());
	}
}
