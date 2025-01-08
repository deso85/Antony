package bot.antony.commands.antcheck;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Controller to regularly check if ant species are available and to infom user
 */
public class AntcheckNotificationController {
	private String subdir = "antcheck" + File.separator;
	private String anrFileName = "availabilityNotificationRequests.json";
	private List<AvailabilityNotificationRequest> anrs = new ArrayList<AvailabilityNotificationRequest>();
	private LocalDateTime lastCheckDateTime = LocalDateTime.now();
	private LocalDateTime nextCheckDateTime = LocalDateTime.now().minusDays(1);
	private boolean isRunning = false;

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AntcheckNotificationController() {
		loadChecks();
		Antony.getLogger().info("Created antcheck availability controller.");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public void checkAvailability() {
		/*if(nextCheckDateTime.isBefore(LocalDateTime.now())) {

			Antony.getLogger().info("[Antcheck Availability Controller] Found " + anrs.size() + " notification requests to check.");
			int newSpecieOffers = 0;
			List<AvailabilityNotificationRequest> anrsCopy = new ArrayList<AvailabilityNotificationRequest>();
			anrsCopy.addAll(anrs);
			
			for(AvailabilityNotificationRequest anr : anrsCopy) {
				//Check if ants are available
				if(Antony.getAntcheckController().getOffersForAntWithoutBlShops(anr.getAnt()).size() > 0) {
					//get member to inform
					Guild guild = Antony.getJda().getGuildById(anr.getGuild().getId());
					List<Member> member = getMemberByANR(guild, anr.getUser());
					
					//send message to inform user
					if(member.size() > 0 &&
							guild.getTextChannelById(anr.getGuild().getCommandsChannelID()) != null) {
						informUser(guild.getTextChannelById(anr.getGuild().getCommandsChannelID()), anr.getAnt(), member);
					}
					
					//delete availability notification request
					anrs.remove(anr);
					saveChecks();
					newSpecieOffers++;
				}
			}
			Antony.getLogger().info("[Antcheck Availability Controller] " + newSpecieOffers + " new Offers found.");
			
			lastCheckDateTime = LocalDateTime.now();
			nextCheckDateTime = LocalDateTime.now().plusMinutes(60).truncatedTo(ChronoUnit.HOURS);
		}*/
	}
	
	public List<Member> getMemberByANR(Guild guild, List<UserData> user){
		List<Member> member = new ArrayList<Member>();
		
		if(guild != null) {
			for(UserData usr : user) {
				if(guild.getMemberById(usr.getId()) != null) {
					member.add(guild.getMemberById(usr.getId()));
				}
			}
		}
		
		return member;
	}
	
	public void informUser(TextChannel textChan, Specie ant, List<Member> member) {
		String notificationText = "\nDie Ameisenart ***" + ant.getName() + "*** scheint nun in Shops verfügbar zu sein."
				+ "\nBitte nutze für weitere Details den Befehl: ***" + Antony.getCmdPrefix() + "sells " + ant.getName() + "***";
		StringBuilder sb = new StringBuilder();
		for(Member mem : member) {
			if((sb.length() + mem.getAsMention().length()) > 2000) {
				textChan.sendMessage(sb.toString()).queue();
				sb = new StringBuilder();
			}
			sb.append(mem.getAsMention() + " ");
		}
		if((sb.length() + notificationText.length()) <= 2000) {
			textChan.sendMessage(sb.toString() + notificationText).queue();
		} else {
			textChan.sendMessage(sb.toString()).queue();
			textChan.sendMessage(notificationText).queue();
		}
	}
	
	public void addAvailabilityCheck(Specie ant, GuildData guild, UserData user) {
		for(AvailabilityNotificationRequest anr : anrs) {
			//check if availability notification request for this ant already exists
			if(anr.getAnt().equals(ant)) {
				if(!anr.getUser().contains(user)) {
					anr.addUser(user);
					saveChecks();
				}
				return;
			}
		}
		//create availability notification request and add user
		anrs.add(new AvailabilityNotificationRequest(ant, guild, user));
		saveChecks();
	}
	
	@SuppressWarnings("unchecked")
	private void loadChecks() {
		anrs = (List<AvailabilityNotificationRequest>) Utils.loadJSONData(subdir, anrFileName, new TypeReference<List<AvailabilityNotificationRequest>>(){}, anrs);
	}
	
	private void saveChecks() {
		Utils.saveJSONData(subdir, anrFileName, anrs);
	}
	
	public void run() {
		if(!isRunning) {
			isRunning = true;
			Antony.getLogger().info("[Antcheck Availability Controller] Starting Runner");
			Thread timerThread = new Thread() {
				public void run() {
					while(Antony.getJda().getPresence().getStatus() == OnlineStatus.ONLINE
							&& anrs.size() > 0) {
						try {
							checkAvailability();
							Thread.sleep(60000); //1min
						} catch (InterruptedException e) {
							Antony.getLogger().error("Wasn't able to put Thread asleep.", e);
						}
					}
					isRunning = false;
					Antony.getLogger().info("[Antcheck Availability Controller] Stopping Runner");
				}
			};
			timerThread.start();
		}
	}

	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public LocalDateTime getLastCheckDateTime() {
		return lastCheckDateTime;
	}
	
}