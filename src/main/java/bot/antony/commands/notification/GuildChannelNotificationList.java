package bot.antony.commands.notification;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bot.antony.Antony;
import bot.antony.guild.GuildData;
import bot.antony.guild.channel.ChannelData;

/**
 * GuildChannelNotificationList stores a List with all discord servers including the ChannelNotificationLists
 */
public class GuildChannelNotificationList {
	private GuildData guild = new GuildData();
	private Map<String, ChannelNotificationList> cnls = new HashMap<String, ChannelNotificationList>();
	

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public GuildChannelNotificationList() {
		super();
	}
	
	public GuildChannelNotificationList(GuildData guild) {
		this.guild = guild;
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------		
	/**
	 * Adds a CNL to the GCNL if necessary
	 * @param	channel as ChannelData
	 * @return	TRUE if CNL has been added or FALSE if CNL already was listed
	 */
	public boolean addCNL(ChannelData channel) {
		//if CNL isn't already on the list
		if(!hasCNL(channel)) {
			getCNLs().put(channel.getId(), new ChannelNotificationList(channel));
			Antony.getLogger().debug("Added CNL [" + channel.toString() + "] to GCNL [" + toString() + "].");
			return true;
		}
		Antony.getLogger().debug("Can't add CNL [" + channel.toString() + "] to GCNL [" + toString() + "] because it's alredy listed.");
		return false;
	}
	
	/**
	 * Removes CNL from GCNL if necessary
	 * @param	channel as ChannelData
	 * @return	TRUE if CNL was removed from list or FALSE if CNL was not empty or not listed
	 */
	public boolean removeCNL(ChannelData channel) {
		if(hasCNL(channel)) {
			getCNLs().get(channel.getId()).clear(); //clears CNL before removing it from GCNL
			getCNLs().remove(channel.getId());
			Antony.getLogger().debug("Removed CNL [" + channel.toString() +"]");
			return true;
		}
		return false;
	}
	
	/**
	 * Returns CNL if available
	 * @param	channel as ChannelData
	 * @return	ChannelNotificationList
	 */
	public ChannelNotificationList getCNL(ChannelData channel) {
		if(hasCNL(channel)) {
			return getCNLs().get(channel.getId());
		}
		return null;
	}
	
	/**
	 * Checks if CNL is listed
	 * @param	channel as ChannelData
	 * @return	TRUE if CNL is listed or FALSE if not
	 */
	public boolean hasCNL(ChannelData channel) {
		return getCNLs().containsKey(channel.getId());
	}
	
	/**
	 * Checks if GCNL is empty
	 * @return	TRUE if is empty or FALSE if not
	 */
	@JsonIgnore
	public boolean isEmpty() {
		return getCNLs().isEmpty();
	}
	
	/**
	 * Clears GCNL
	 */
	public void clear() {
		for(HashMap.Entry<String, ChannelNotificationList> cnlEntry: getCNLs().entrySet()) {
			cnlEntry.getValue().clear();
		}
		getCNLs().clear();
	}
	
	/**
	 * Prints all CNLs which exist for the Discord server
	 */
	public void printCNLs() {
		if(!isEmpty()) {
			StringBuilder logEntry = new StringBuilder();
			logEntry.append("CNLs inside GCNL [" + toString() + "]: ");
			int counter = 1;
			for(HashMap.Entry<String, ChannelNotificationList> cnlEntry : getCNLs().entrySet()) {
				ChannelNotificationList cnl = cnlEntry.getValue();
				logEntry.append("[" + cnl.toString() + "]");
				if(counter < getCNLs().size()) {
					logEntry.append(", ");
					counter++;
				}
			}
			Antony.getLogger().info(logEntry.toString());
		} else {
			Antony.getLogger().info("GCNL [" + toString() + "] is empty.");
		}
	}
	
	@Override
	public String toString() {
		return "id:" + getId() + ", name:" + getName();
	}
	
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	@JsonIgnore
	public String getId() {
		return guild.getId();
	}

	@JsonIgnore
	public void setId(String id) {
		this.guild.setId(id);
	}
	
	@JsonIgnore
	public String getName() {
		return guild.getName();
	}

	@JsonIgnore
	public void setName(String name) {
		this.guild.setName(name);
	}
	
	public GuildData getGuild() {
		return this.guild;
	}
	
	public Map<String, ChannelNotificationList> getCNLs() {
		return cnls;
	}
}