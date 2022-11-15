package bot.antony.commands.giveaway;

import java.time.Instant;

import net.dv8tion.jda.api.entities.Message;

/**
 * Class to store and load Giveaway data by the {@link bot.antony.controller.GiveawayController GiveawayController}.
 * 
 * @since  7.6.0
 * @author deso85
 */
public class Giveaway {
	String sponsorID;
	String sponsorName;
	String description;
	String guildID;
	long chanID;
	long messageID;
	long gaEndEpochSeconds;
	int winCount;
	
	/**
	 * Constructs a new Giveaway instance without any data
	 */
	public Giveaway() {
		super();
	}
	
	/**
	 * Constructs a new Giveaway instance with all required data
	 * 
	 * @param  sponsorID
	 *         Unique user/member ID given by Discord. Identifies the sponsor of the Giveaway.
	 * @param  sponsorName
	 *         The sponsors known name. Will be stored in case the sponsor leaves the Discord Guild and therefore can't be recognized.
	 * @param  description
	 *         The Giveaways description with all details
	 * @param  message
	 *         Message which includes the related Guild, Channel and the ID, to later change it
	 * @param  runtimeMin
	 *         Amount of minutes the Giveaway will run
	 * @param  winCount
	 *         Amount of people who can win the Giveaway
	 */
	public Giveaway(String sponsorID, String sponsorName, String description, Message message, int runtimeMin, int winCount) {
		super();
		this.sponsorID = sponsorID;
		this.sponsorName = sponsorName;
		this.description = description;
		this.guildID = message.getGuild().getId();
		this.chanID = message.getChannel().getIdLong();
		this.messageID = message.getIdLong();
		this.gaEndEpochSeconds = Instant.now().getEpochSecond() + runtimeMin*60;
		this.winCount = winCount;
	}
	
	/**
	 * Checks if the Giveaway has ended
	 * 
	 * @return boolean
	 */
	public boolean hasEnded() {
		if(Instant.now().getEpochSecond() > gaEndEpochSeconds) {
			return true;
		}
		return false;
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public String getSponsorID() {
		return sponsorID;
	}
	public void setSponsorID(String sponsorID) {
		this.sponsorID = sponsorID;
	}	
	public String getSponsorName() {
		return sponsorName;
	}
	public void setSponsorName(String sponsorName) {
		this.sponsorName = sponsorName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public long getMessageID() {
		return messageID;
	}
	public void setMessageID(long messageID) {
		this.messageID = messageID;
	}
	public long getGaEndEpochSeconds() {
		return gaEndEpochSeconds;
	}
	public void setGaEndEpochSeconds(long gaEndEpochSeconds) {
		this.gaEndEpochSeconds = gaEndEpochSeconds;
	}
	public int getWinCount() {
		return winCount;
	}
	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}
	
}
