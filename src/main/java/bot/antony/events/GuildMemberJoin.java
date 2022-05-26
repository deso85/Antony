package bot.antony.events;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import bot.antony.Antony;
import bot.antony.controller.UserController;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMemberJoin extends ListenerAdapter {

	Guild guild;
	Member member;
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		UserController usrCntrl = Antony.getUserController();
		guild = event.getGuild();
		member = event.getMember();
	    GuildData guildData = new GuildData(guild);
	    UserData userData = usrCntrl.loadUserData(member);
	    userData.setLastOnline(System.currentTimeMillis());
	    usrCntrl.saveUserData(userData, guild);
	    
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("User [" + userData.toString() + "] ");
	    logMessage.append("joined Discord server [" + guildData.toString() + "].");
		
		//update usercount
	    Antony.setUsercount(Antony.getUsercount()+1);
	    event.getJDA().getPresence().setActivity(Activity.listening(Antony.getCmdPrefix() + "antony | " + Antony.getUsercount() + " User | " + event.getJDA().getGuilds().size() + " Server"));
	    
	    //Welcome user to the server
	    TextChannel welcomeChannel = Antony.getGuildController().getWelcomeChannel(guild);
		if(welcomeChannel != null) {
			welcomeChannel.sendMessage(getRandomWelcomeText()).complete();
		}
	    
		//Check how young the account is and give an info if it's younger than 30 days
		if(Antony.getGuildController().getLogChannel(guild) != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
			LocalDate passedDate = LocalDate.parse(member.getTimeCreated().atZoneSameInstant(ZoneId.systemDefault()).format(formatter), formatter);
			if(passedDate.isAfter(LocalDate.now().minusDays(30))) {
				StringBuilder sb = new StringBuilder();
				sb.append("ID: " + member.getId() + "\n");
				sb.append("Tag: " + member.getUser().getAsTag() + "\n");
				sb.append("Name: " + member.getUser().getName() + "\n");
				if(member.getNickname() != null) {
					sb.append("Nickname: " + member.getNickname() + "\n");
				}
				sb.append("Discord beigetreten: " + member.getTimeCreated().atZoneSameInstant(ZoneId.systemDefault()).format(formatter) + " Uhr");
				Antony.getGuildController().getLogChannel(guild).sendMessage("ℹ️ Neu gejointer Account ist jünger als 30 Tage").complete();
				Antony.getGuildController().getLogChannel(guild).sendMessage(member.getUser().getEffectiveAvatarUrl() + "?size=2048").complete();
				Antony.getGuildController().getLogChannel(guild).sendMessage(sb.toString()).complete();
			}
		}
		
	    Antony.getLogger().info(logMessage.toString());
	}
	
	public String getRandomWelcomeText() {
		//TODO: Usefull for every guild? -> add to GuildData
		TextChannel activation = Antony.getGuildController().getActivationRulesChannel(guild);
		ArrayList<String> messages = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("Hey " + member.getAsMention() + ", das AAM-Team und die gesamte Community heißen dich herzlich auf dem Server **" + guild.getName() + "** willkommen!\n"
				+ "Du bist übrigens schon das " + guild.getMembers().size() + ". Mitglied.");
		if(activation != null) {
			sb.append("\nDu kannst dich schonmal umsehen - bitte wirf einen kurzen Blick in " + activation.getAsMention() + ", um hier bald selbst loslegen zu können!");
		}
		sb.append("\n\nViel Spaß!");
		
		
		messages.add(sb.toString());
		
		return messages.get((int) (System.currentTimeMillis() % messages.size()));
	}
}
