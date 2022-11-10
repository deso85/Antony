package bot.antony.commands.giveaway;

import java.time.Instant;

import net.dv8tion.jda.api.entities.Message;

public class Giveaway {
	String sponsorID;
	String sponsorName;
	String description;
	String guildID;
	long chanID;
	long msgID;
	long gaEndEpochSeconds;
	int winCount;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public Giveaway() {
		super();
	}
	public Giveaway(String sponsorID, String sponsorName, String description, Message msg, int runtimeMin, int winCount) {
		super();
		this.sponsorID = sponsorID;
		this.sponsorName = sponsorName;
		this.description = description;
		this.guildID = msg.getGuild().getId();
		this.chanID = msg.getChannel().getIdLong();
		this.msgID = msg.getIdLong();
		this.gaEndEpochSeconds = Instant.now().getEpochSecond() + runtimeMin*60;
		this.winCount = winCount;
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
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
	public long getMsgID() {
		return msgID;
	}
	public void setMsgID(long msgID) {
		this.msgID = msgID;
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
