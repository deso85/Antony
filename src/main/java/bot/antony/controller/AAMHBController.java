package bot.antony.controller;

import java.awt.Color;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

/**
 * This controller is specific for the AAM discord server
 * It shall control channels and check if there are regular updates inside channels owned by users
 */
public class AAMHBController {

	private Guild guild;
	private LocalDate lastChecked;
	private String overdueListSubDir;
	private String overdueListFileName;
	private String lastCheckedFileName;
	private Map<Long, String> overdueList = new HashMap<Long, String>();
	private Map<Long, List<TextChannel>> memChans = new HashMap<Long, List<TextChannel>>();
	private List<TextChannel> relChans = new ArrayList<TextChannel>();
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AAMHBController() {
		Antony.getLogger().info("Created AAM HB controller.");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@SuppressWarnings("unchecked")
	public void checkHBs() {
		LocalDate currentDate = LocalDate.now();
		Period period = Period.between(lastChecked, currentDate);
		
		//Check shall run every 7 days
		if((period.getDays() > 6
				|| period.getMonths() > 0
				|| period.getYears() > 0)
				&& guild != null) {
			Antony.getLogger().info("[AAM HB Controller] Start AAM HB check");
			lastChecked = currentDate;
			Utils.saveJSONData(overdueListSubDir, lastCheckedFileName, lastChecked);
			setRelevantChannels();
			
			if(!relChans.isEmpty()) {
				Antony.getLogger().debug("[AAM HB Controller] There are relevant channels");
				//load overdues
				overdueList = (Map<Long, String>) Utils.loadJSONData(overdueListSubDir, overdueListFileName, new TypeReference<Map<Long, String>>(){}, overdueList);
				trimOverdueList(); //remove all channels which aren't available anymore
				Antony.getLogger().debug("[AAM HB Controller] Trimmed overdue list");
				
				//update overdueList
				for(TextChannel chan : relChans) {
					if(overdueList.containsKey(chan.getIdLong())) {
						overdueList.put(chan.getIdLong(), overdueList.get(chan.getIdLong()) + ";" + currentDate.toString());
					} else {
						overdueList.put(chan.getIdLong(), currentDate.toString());
					}
				}
				Antony.getLogger().debug("[AAM HB Controller] Updated overdue list");
				
				//write everyone
				Map<Member, List<TextChannel>> reportList = new HashMap<Member, List<TextChannel>>();
				for(Map.Entry<Long, List<TextChannel>> entry : memChans.entrySet()) {
					//1L is not a real member. It's used as a place holder for channels without any content
					if(entry.getKey() != 1L) {
						Member member = guild.getMemberById(entry.getKey());
						List<TextChannel> reportTextChannel = new ArrayList<TextChannel>(); //List gets entrys if more than 3 reminders have been sent
						
						//Check if any of the channels have been mentioned too many times
						for(TextChannel chan : entry.getValue()) {
							if(overdueList.get(chan.getIdLong()).split(";").length > 3) {
								reportTextChannel.add(chan);
							}
						}
						//if there are too many mentions moderators will be informed
						if(!reportTextChannel.isEmpty()) {
							reportList.put(member, reportTextChannel);
						}
						//tell member to update channel
						sendPN(member, entry.getValue());
					}
				}
				
				//report to mods
				reportHBs(reportList);
				
				//save overdues
				Utils.saveJSONData(overdueListSubDir, overdueListFileName, overdueList);
			} else {
				Antony.getLogger().debug("[AAM HB Controller] There are no relevant channels");
			}
			
			
		}
	}
	
	private void reportHBs(Map<Member, List<TextChannel>> reportList) {
		if(Antony.getGuildController().getLogChannel(guild) != null) {
			StringBuilder sb = new StringBuilder();
			
			//intro
			sb.append("Die heutige Prüfung der HBs ist abgeschlossen.");
			
			//there are channels to report
			if(memChans.containsKey(1L)
					|| !reportList.isEmpty()) {
				//no authors
				if(memChans.containsKey(1L)) {
					sb.append("\n\nFolgende Kanäle sind älter als 2 Wochen und haben keine Inhalte oder der Author ist nicht mehr auf dem Server:");
					for(TextChannel chan : memChans.get(1L)) {
						sb.append("\n- " + chan.getAsMention());
						if(sb.length() >= 1900) {
							Antony.getGuildController().getLogChannel(guild).sendMessage(sb.toString()).queue();
							sb = new StringBuilder();
						}
					}
				}
				//authors
				if(!reportList.isEmpty()) {
					sb.append("\n\nFolgende Kanäle sind älter als 6 Monate und der Author wurde mindestens 3 mal erinnert:");
					for(Map.Entry<Member, List<TextChannel>> entry : reportList.entrySet()) {
						sb.append("\n" + entry.getKey().getAsMention() + ": ");
						int counter = 1;
						for(TextChannel chan : entry.getValue()) {
							sb.append(chan.getAsMention());
							if(counter < entry.getValue().size()) {
								sb.append(", ");
								counter++;
							}
						}
						if(sb.length() >= 1500) {
							Antony.getGuildController().getLogChannel(guild).sendMessage(sb.toString()).queue();
							sb = new StringBuilder();
						}
					}
				}
				
			} else {
				sb.append("Wenn es Auffälligkeiten gegeben hat, wurden die User durch mich angeschrieben.\n"
						+ "Die betreffenden User und Kanäle werden erst nach der 3. Aufforderung durch mich, ein Update zu schreiben, hier gemeldet.\n"
						+ "Bis dahin sollten keine weiteren Schritte unternommen werden.");
			}
			sb.append("\n\nDie nächste Prüfung findet in 7 Tagen statt.");
			Antony.getGuildController().getLogChannel(guild).sendMessage(sb.toString()).queue();
		}
	}
	
	private void sendPN(Member member, List<TextChannel> chans) {
		Antony.getLogger().debug("[AAM HB Controller] sending PN to " + member.getAsMention());
		StringBuilder sb = new StringBuilder();
		sb.append("Hallo " + member.getEffectiveName() + ",\n");
		sb.append("ich habe heute alle Haltungsberichte auf dem Server **" + guild.getName() + "** überprüft und dabei leider festgestellt, dass du schon lange keine Updates mehr in folgenden Kanälen geschrieben hast:");
		for(TextChannel chan : chans) {
			sb.append("\n- " + chan.getAsMention());
		}
		sb.append("\n\nEin gut geführter Haltungsbericht, der häufig aktualisiert wird und viele Details enthält, ist die beste Erinnerung für dich und sorgt dafür, dass er von vielen Usern gelesen wird.");
		sb.append("\nSolltest du mehrere Male von mir erinnert werden, wird auch die Server-Moderation informiert und dann kann es passieren, dass der Kanal gelöscht wird.");
		sb.append("\nWenn du den HB nicht mehr fortführen möchtest, informiere doch bitte jemanden vom AAM-Team hierüber.");
		sb.append("\n\nIch hoffe, ich konnte dich motivieren, bald ein neues Update zu verfassen.\nViele Grüße");
		try {
			member.getUser().openPrivateChannel().complete().sendMessage(sb.toString()).complete();
		} catch (ErrorResponseException e) {
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
			
			TextChannel channel = Antony.getGuildController().getLogChannel(member.getGuild());
			
			if(channel != null) {
				channel.sendMessage(":postbox: Fehler bei der Zustellung einer privaten Nachricht.").complete();
				EmbedBuilder eb = new EmbedBuilder()
						.setColor(Color.red)
						.setAuthor(member.getUser().getAsTag() + " | ID: " + member.getId(), null, member.getUser().getAvatarUrl())
						.setDescription("Ich konnte keine PN an den User " + member.getAsMention() + " senden. Es ist sehr wahrscheinlich, dass seine Privatsphäre-Einstellungen einen direkten Versand an ihn verhindern. "
								+ "Bitte informiert ihn hierüber, damit er die passenden Einstellungen setzen oder die Benachrichtigungen deaktivieren kann.\n\n"
								+ "Hier finden sich Hintergrundinformationen zu dem Thema:\n"
								+ "https://support.discord.com/hc/de/articles/217916488-Blocken-Datenschutzeinstellungen")
						.setFooter(now.format(formatter) + " Uhr");
				channel.sendMessageEmbeds(eb.build()).complete();
			}
			
			Antony.getLogger().error("ErrorResponseException: Wasn't able to send PN to User " + member.getUser().getAsTag() + " (ID " + member.getId() + ")");
		}
	}
	
	private void trimOverdueList(){
		Map<Long, String> newList = new HashMap<Long, String>();
		newList.putAll(overdueList);
		for (Map.Entry<Long, String> entry : overdueList.entrySet()) {
			if(!getChanIDs(relChans).contains(entry.getKey())) {
				newList.remove(entry.getKey());
			}
		}
		overdueList = newList;
	}
	
	private void setRelevantChannels() {
		List<TextChannel> chans = new ArrayList<TextChannel>();
		
		//get all channels which are in the correct categories
		for(Category cat : guild.getCategories()) {
			if(cat.getName().toLowerCase().contains("hb")
					&& !cat.getName().toLowerCase().contains("geschlossen")) {
				chans.addAll(cat.getTextChannels());
			}
		}
		
		//remove chat channels
		chans = chans.stream()
				.filter(chan -> !chan.getName().contains("chat-hb"))
				.collect(Collectors.toList());
		
		//romve channels which are up to date
		for(TextChannel chan : chans) {
			MessageHistory history = new MessageHistory(chan);
			List<Message> messageList = history.retrievePast(1).complete();
			//Check if channel is empty and older than 2 Weeks
			if((messageList.isEmpty() || messageList.get(0).getAuthor().isBot())
					&& chan.getTimeCreated().plusWeeks(2).isBefore(OffsetDateTime.now())
					|| messageList.get(0).getMember() == null) {
				relChans.add(chan);
				if(memChans.containsKey(1L)) {
					List<TextChannel> chanUpdate = new ArrayList<TextChannel>(memChans.get(1L));
					chanUpdate.add(chan);
					memChans.put(1L, chanUpdate);
				} else {
					memChans.put(1L, Arrays.asList(chan));
				}
			} else if(!messageList.isEmpty() && messageList.get(0).getTimeCreated().plusMonths(6).isBefore(OffsetDateTime.now())) {
				Antony.getLogger().debug("[AAM HB Controller] Channel with Author: " + messageList.get(0).getAuthor().getName() + " " + messageList.get(0).getAuthor().getId() ); 
				relChans.add(chan);
				//TODO: is it a problem if author isn't on the server anymore?
				if(memChans.containsKey(messageList.get(0).getAuthor().getIdLong())) {
					List<TextChannel> chanUpdate = new ArrayList<TextChannel>(memChans.get(messageList.get(0).getAuthor().getIdLong()));
					chanUpdate.add(chan);
					memChans.put(messageList.get(0).getAuthor().getIdLong(), chanUpdate);
				} else {
					memChans.put(messageList.get(0).getAuthor().getIdLong(), Arrays.asList(chan));
				}
			}
		}
		
		Antony.getLogger().info("[AAM HB Controller] found " + relChans.size() + " channels to check.");
	}
	
	private ArrayList<Long> getChanIDs(List<TextChannel> chans) {
		ArrayList<Long> chanIDs = new ArrayList<Long>();
		
		for(TextChannel chan : chans) {
			chanIDs.add(chan.getIdLong());
		}
		
		return chanIDs;
	}
	
	public void setVars(JDA jda) {
		//set guild if necessary
		if(guild == null) {
			Antony.getLogger().info("[AAM HB Controller] No guild set... searching for the right one.");
			if(!findGuild(jda)) {
				return;
			}
		}
		
		overdueListSubDir = "guilds" + File.separator + guild.getId() + " - " + guild.getName() + File.separator;
		overdueListFileName = "hb.overdues.json";
		lastCheckedFileName = "hb.lastchecked.json";
		lastChecked = LocalDate.of(2000, 1, 1);
		lastChecked = (LocalDate) Utils.loadJSONData(overdueListSubDir, lastCheckedFileName, new TypeReference<LocalDate>(){}, lastChecked);
		
	}
	
	private boolean findGuild(JDA jda) {
		for(Guild iguild : jda.getGuilds()) {
			if(iguild.getName().contains("AAM")
					|| iguild.getName().contains("Ameisen an die Macht!")) {
				guild = iguild;
				Antony.getLogger().info("[AAM HB Controller] Found guild " + iguild.getName() + "(ID: " + iguild.getId() + ") and set it as the guild which has to be checked.");
				return true;
			}
		}
		return false;
	}
}
