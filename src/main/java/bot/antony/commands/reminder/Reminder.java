package bot.antony.commands.reminder;

import java.time.Instant;

import net.dv8tion.jda.api.entities.Message;

/**
 * Class to store and load Reminder data by the {@link bot.antony.controller.ReminderController ReminderController}.
 * 
 * @since  7.7.0
 * @author deso85
 */
public class Reminder {
	long id;
	String userID;
	String reminderText;
	String guildID;
	long chanID;
	long endEpochSeconds;
	
	/**
	 * Constructs a new Reminder instance without any data
	 */
	public Reminder() {
		super();
		this.id = Instant.now().getEpochSecond();
	}
	
	/**
	 * Constructs a new Reminder instance with all required data
	 * 
	 * @param  message
	 *         Message which includes all relevant information except the runtime
	 * @param  runtimeMin
	 *         Amount of minutes the Reminder will last
	 */
	public Reminder(Message message, int runtimeMin) {
		super();
		this.id = Instant.now().getEpochSecond();
		this.userID = message.getAuthor().getId();
		this.reminderText = message.getContentRaw().replaceAll("\\d+[dhm]", "").replaceFirst("!reminder", "").trim();
		if(this.reminderText.startsWith("!")) {
			this.reminderText = this.reminderText.replaceFirst("!", "").trim();
		}
		this.guildID = message.getGuild().getId();
		this.chanID = message.getChannel().getIdLong();
		this.endEpochSeconds = Instant.now().getEpochSecond() + runtimeMin*60;
	}
	
	/**
	 * Checks if the Reminder time has come
	 * 
	 * @return boolean
	 */
	public boolean hasEnded() {
		if(Instant.now().getEpochSecond() > endEpochSeconds) {
			return true;
		}
		return false;
	}

	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getReminderText() {
		return reminderText;
	}

	public void setReminderText(String reminderText) {
		this.reminderText = reminderText;
	}

	public String getGuildID() {
		return guildID;
	}

	public void setGuildID(String guildID) {
		this.guildID = guildID;
	}

	public long getChanID() {
		return chanID;
	}

	public void setChanID(long chanID) {
		this.chanID = chanID;
	}

	public long getEndEpochSeconds() {
		return endEpochSeconds;
	}

	public void setEndEpochSeconds(long endEpochSeconds) {
		this.endEpochSeconds = endEpochSeconds;
	}
	
}
