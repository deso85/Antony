package bot.antony.events.reaction.add;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.guild.user.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class RedFlagReaction extends MessageReaction {

	List<User> userList;
	List<String> blockedRoles;
	int flagsToDeleteMessage;
	int usrCount;
	DeletionPair delPair = new DeletionPair();
	String deletionPairFileName;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public RedFlagReaction(MessageReactionAddEvent event) {
		super(event);
		allowedRoles = new ArrayList<>(Arrays.asList("everyone"));
		blockedRoles = new ArrayList<>(Arrays.asList("Ei", "2nd 🎤"));
		responseChannel = Utils.getLogChannel(guild, message.getTextChannel());
		userList = event.getReaction().retrieveUsers().complete();
		if(Antony.isProdStage()) {
			flagsToDeleteMessage = 5;
		} else {
			flagsToDeleteMessage = 1;
		}
		deletionPairFileName = "antony.deletionpair.json";
		loadDeletionPair();
		usrCount = getNormalizedUserCount();
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void play() {
		UserData usrData = new UserData(reactor);
		logMessage.append("User [" + usrData.toString() + "] REDFLAGGED "
				+ "message [" + message.getId() + "] "
				+ "in channel #" + message.getChannel().getName() + " (CID: " + message.getChannel().getId() + ").");
		
		printHeader();
		printMessageEmbed();
		if(usrCount >= flagsToDeleteMessage) {
			//If last delete is 15min ago the counter gets reset
			if(delPair.getDelCount() > 0 && delPair.getLastDeleted().before(new Date(System.currentTimeMillis() - 900 * 1000))) {
				delPair.setDelCount(0);
				saveDeletionPair();
			}
			if(delPair.getDelCount() < 3) {
				logMessage.append("\nBecause there were " + getNormalizedUserCount() + " authorized flaggs the message has been deleted.");
				printAttachments();
				deleteMessage();
			} else {
				logMessage.append("\nBecause there were " + getNormalizedUserCount() + " authorized flaggs the message should have been deleted but there are too many deleted messages within the last 15min.");
				responseChannel.sendMessage("❗ " + getNormalizedUserCount() + " berechtigte User haben die Nachricht markiert, sie wurde aber **nicht** gelöscht, weil zu viele Nachrichten in den letzten 15min gelöscht wurden. **Bitte dringend prüfen, ob jemand das System ausnutzt!**").complete();
			}
		}
		log();
	}
	
	public void printHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append(emote.getEmote().getAsMention() + " **" + userList.size() + "**\n");
		sb.append("Markiert von: ");
		
		int counter = 1;
		for(User user: userList) {
			if(reactor.getUser().equals(user)) {
				sb.append("__");
			}
			sb.append(user.getAsTag() + " (" + user.getId() + ")");
			if(reactor.getUser().equals(user)) {
				sb.append("__");
			}
			if(counter < userList.size()) {
				sb.append(", ");
				counter++;
			}
		}
		
		responseChannel.sendMessage(sb.toString()).complete();
	}
	
	public void printMessageEmbed() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		EmbedBuilder eb = new EmbedBuilder()
				.setColor(Color.red)
				.setAuthor(message.getAuthor().getAsTag() + " | ID: " + message.getAuthor().getId(), null, message.getAuthor().getAvatarUrl())
				.setDescription(message.getContentDisplay())
				.addField("#" + message.getChannel().getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + guild.getId() + "/" + message.getChannel().getId() + "/" + message.getId() + ")**", false)
				.setFooter(formatter.format(date));
		responseChannel.sendMessageEmbeds(eb.build()).complete();
	}
	
	public void deleteMessage() {
		delPair.setDelCount(delPair.getDelCount()+1);
		delPair.setDelSum(delPair.getDelSum()+1);
		delPair.setLastDeleted(new Date(System.currentTimeMillis()));
		saveDeletionPair();
		message.delete().complete();
		responseChannel.sendMessage("❗ " + getNormalizedUserCount() + " berechtigte User haben die Nachricht markiert, weshalb sie entfernt wurde.").complete();
	}
	
	public int getNormalizedUserCount() {
		int userCount = 0;
		for(User user : userList) {
			if(guild.getMember(user).getRoles().size() > 0 && !Utils.memberHasRole(guild.getMember(user), blockedRoles)) {
				userCount++;
			}
		}
		return userCount;
	}
	
	public void saveDeletionPair() {
		Utils.storeData(deletionPairFileName, delPair);
	}
	
	public void loadDeletionPair() {
		try {
			delPair = (DeletionPair) Utils.loadData(deletionPairFileName, new TypeReference<DeletionPair>(){}, delPair);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class DeletionPair {
	int delCount;
	int delSum;
	Date lastDeleted;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public DeletionPair() {
		super();
		delCount = 0;
		delSum = 0;
		lastDeleted = new Date(System.currentTimeMillis() - 900 * 1000);
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public int getDelCount() {
		return delCount;
	}
	
	public void setDelCount(int delCount) {
		this.delCount = delCount;
	}
	
	public int getDelSum() {
		return delSum;
	}

	public void setDelSum(int delSum) {
		this.delSum = delSum;
	}

	public Date getLastDeleted() {
		return lastDeleted;
	}
	
	public void setLastDeleted(Date lastDeleted) {
		this.lastDeleted = lastDeleted;
	}
}
