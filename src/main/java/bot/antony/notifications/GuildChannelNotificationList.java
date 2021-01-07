package bot.antony.notifications;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bot.antony.Antony;

/**
 * GuildChannelNotificationList stores a List with all discord servers including the ChannelNotificationLists
 */
public class GuildChannelNotificationList {
	private GuildData gd = new GuildData();
	private Map<String, ChannelNotificationList> cnls = new HashMap<String, ChannelNotificationList>();
	

	public GuildChannelNotificationList() {
		super();
	}


	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public GuildChannelNotificationList(GuildData gd) {
		this.gd = gd;
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------		
	/**
	 * Adds a CNL to the GCNL if necessary
	 * @param	cd as ChannelData
	 * @return	TRUE if CNL has been added or FALSE if CNL already was listed
	 */
	public boolean addCNL(ChannelData cd) {
		//if CNL isn't already on the list
		if(!hasCNL(cd.getId())) {
			getCNLs().put(cd.getId(), new ChannelNotificationList(cd));
			Antony.getLogger().debug("Added CNL " + cd.getId() + " (" + cd.getName() + ") to GCNL " + getID() + " (" + getName() + ").");
			return true;
		}
		Antony.getLogger().debug("Can't add CNL " + cd.getId() + " (" + cd.getName() + ") to GCNL " + getID() + " (" + getName() + ") because it's alredy on the list.");
		return false;
	}
	
	/**
	 * Removes CNL from GCNL if necessary
	 * @param	cd as ChannelData
	 * @return	TRUE if CNL was removed from list or FALSE if CNL was not empty or not listed
	 */
	public boolean removeCNL(ChannelData cd) {
		if(hasCNL(cd.getId())) {
			getCNLs().get(cd.getId()).clear(); //clears CNL before removing it from GCNL
			getCNLs().remove(cd.getId());
			Antony.getLogger().debug("Removed CNL " + cd.getId() + "(" + cd.getName() +")");
			return true;
		}
		return false;
	}
	
	/**
	 * Returns CNL if available
	 * @param	channelID as String
	 * @return	ChannelNotificationList
	 */
	public ChannelNotificationList getCNL(String channelID) {
		if(hasCNL(channelID)) {
			return getCNLs().get(channelID);
		}
		return null;
	}
	
	/**
	 * Checks if CNL is listed
	 * @param	channelID as String
	 * @return	TRUE if CNL is listed or FALSE if not
	 */
	public boolean hasCNL(String channelID) {
		return getCNLs().containsKey(channelID);
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
		for(String key: getCNLs().keySet()) {
			getCNL(key).clear();
		}
		getCNLs().clear();
	}
	
	/**
	 * Prints all CNLs which exist for the Discord server
	 */
	public void printCNLs() {
		if(!isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("CNLs inside GCNL " + getID() + " (" + getName() + "): ");
			int counter = 1;
			for(String key : getCNLs().keySet()) {
				sb.append(key.toString() + " (" + getCNLs().get(key).getName() + ")");
				if(counter < getCNLs().size()) {
					sb.append(", ");
				}
				counter++;
			}
			Antony.getLogger().info(sb.toString());
		} else {
			Antony.getLogger().info("GCNL " + getID() + " (" + getName() + ") is empty.");
		}
	}
	
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public String getID() {
		return gd.getId();
	}

	public void setID(String id) {
		this.gd.setId(id);
	}
	
	public String getName() {
		return gd.getName();
	}

	public void setName(String name) {
		this.gd.setName(name);
	}
	
	public Map<String, ChannelNotificationList> getCNLs() {
		return cnls;
	}
}