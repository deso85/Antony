package bot.antony.events;

import java.util.ArrayList;

import bot.antony.Antony;
import bot.antony.guild.GuildData;
import bot.antony.guild.user.UserData;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMemberJoin extends ListenerAdapter {

	Guild guild;
	Member member;
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		guild = event.getGuild();
		member = event.getMember();
	    User user = event.getUser();
	    GuildData guildData = new GuildData(guild);
	    UserData userData = new UserData(user);
	    
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("User [" + userData.toString() + "] ");
	    logMessage.append("joined Discord server [" + guildData.toString() + "].");
		
		//update usercount
	    Antony.setUsercount(Antony.getUsercount()+1);
	    event.getJDA().getPresence().setActivity(Activity.listening(Antony.getCmdPrefix() + "antony | " + Antony.getUsercount() + " User | " + event.getJDA().getGuilds().size() + " Server"));
	    
	    //Welcome user to the server
	    Long welcomeChannelID;
		if(Antony.isProdStage()) {
			welcomeChannelID = 554261873483055114L;
		} else {
			welcomeChannelID = 778960515895918627L;
		}
		guild.getTextChannelById(welcomeChannelID).sendMessage(getRandomWelcomeText()).queue();
	    
	    Antony.getLogger().info(logMessage.toString());
	}
	
	public String getRandomWelcomeText() {
		TextChannel activation;
		if(Antony.isProdStage()) {
			activation = guild.getTextChannelById(724583172586864690L);
		} else {
			activation = guild.getTextChannelById(778960515895918629L);
		}
		ArrayList<String> messages = new ArrayList<String>();
		
		messages.add("Hey " + member.getAsMention() + ", das AAM-Team und die gesamte Community heißen dich herzlich auf dem Server **" + guild.getName() + "** willkommen!\n"
				+ "Du bist übrigens schon das " + guild.getMembers().size() + ". Mitglied.\n"
				+ "Du kannst dich schonmal umsehen - bitte wirf einen kurzen Blick in " + activation.getAsMention() + ", um hier bald selbst loslegen zu können!\n\nViel Spaß!");
		
		return messages.get((int) (System.currentTimeMillis() % messages.size()));
	}
}
