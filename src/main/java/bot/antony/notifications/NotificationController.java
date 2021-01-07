package bot.antony.notifications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bot.antony.Antony;

/**
 * ChannelNotificationController controls all notifications
 */
public class NotificationController {
	private Map<String, GuildChannelNotificationList> gcnl = new HashMap<String, GuildChannelNotificationList>();
	private String fileName = "antony.notifications.json";
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	
	/**
	 * Toggles user notification for channel
	 * @param	gd as GuildData
	 * @param	cd as ChannelData
	 * @param	userID as String
	 * @return	TRUE if user has been added to notification or FALSE if user has been removed from notifications
	 */
	public boolean toggleNotification(GuildData gd, ChannelData cd, String userID) {
		//if GCNL exists
		if(hasGCNL(gd.getId())) {
			//if GCNL has CNL
			if(getGCNL(gd.getId()).hasCNL(cd.getId())) {
				//if user is listed
				if(getGCNL(gd.getId()).getCNL(cd.getId()).hasUser(userID)) {
					removeNotification(gd, cd, userID);
					return false;
				}
			}
		}
		addNotification(gd, cd, userID);
		return true;
	}
	
	/**
	 * Adds user to notification list if necessary
	 * @param	gd as GuildData
	 * @param	cd as ChannelData
	 * @param	userID as String
	 * @return	TRUE if user has been added or FALSE if not
	 */
	public boolean addNotification(GuildData gd, ChannelData cd, String userID) {
		//functions will take care to not add duplicates
		addGCNL(gd);
		addCNLtoGCNL(gd.getId(), cd);
		return getGCNL(gd.getId()).getCNL(cd.getId()).addUser(userID);
		//persistData();
		//return returnVal;
	}
	
	/**
	 * Removes user from notification list if possible
	 * @param	gd as GuildData
	 * @param	cd as ChanelData
	 * @param	userID as String
	 * @return	TRUE if user has been removed from notification List or FALSE if not
	 */
	public boolean removeNotification(GuildData gd, ChannelData cd, String userID) {
		//if GCNL exists
		if(hasGCNL(gd.getId())) {
			//if GCNL has CNL
			if(getGCNL(gd.getId()).hasCNL(cd.getId())) {
				//remove user if possible
				return getGCNL(gd.getId()).getCNL(cd.getId()).removeUser(userID);
				/*//if CNL is now empty it can be removed
				if(getGCNL(gd.getId()).getCNL(cd.getId()).isEmpty()) {
					getGCNL(gd.getId()).removeCNL(cd);
					//if GCNL is now empty it can be removed
					if(getGCNL(gd.getId()).isEmpty()) {
						removeGCNL(gd.getId());
					}
				}*/
				//cleanGuildLists(gd.getId());
				//return returnVal;
			}
		}
		return false;
	}
	
	/**
	 * Adds GCNL if necessary
	 * @param	gd as GuildData
	 * @return	TRUE if GCNL was added or FALSE if GCNL already was listed
	 */
	private boolean addGCNL(GuildData gd) {
		//check if GCNL isn't already listed
		if(!hasGCNL(gd.getId())) {
			getGCNLs().put(gd.getId(), new GuildChannelNotificationList(gd));
			return true;
		}
		return false;
	}
	
	/**
	 * Returns GCNL if listed
	 * @param	guildID as String
	 * @return	GuildChannelNotificationList
	 */
	public GuildChannelNotificationList getGCNL(String guildID) {
		//check if GCNL exists before returning it
		if(hasGCNL(guildID)) {
			return getGCNLs().get(guildID);
		}
		return null;
	}
	
	/**
	 * Removes GCNL if possible
	 * @param	guildID as String
	 * @return	TRUE if GCNL has been removed or FALSE if not
	 */
	private boolean removeGCNL(String guildID) {
		//check if GCNL exists before clearing and removing it
		if(hasGCNL(guildID)) {
			getGCNL(guildID).clear();
			getGCNLs().remove(guildID);
			return true;
		}
		return false;
	}
	
	/**
	 * Adds a CNL to a GCNL
	 * @param	guildID as String
	 * @param	cd as ChannelData
	 * @return	TRUE if CNL has been added or FALSE if not
	 */
	private boolean addCNLtoGCNL(String guildID, ChannelData cd) {
		//check if GCNL exists before adding the CNL
		if(hasGCNL(guildID)) {
			return getGCNL(guildID).addCNL(cd);
		}
		return false;
	}
	
	/**
	 * Removes CNL from GCNL if possible
	 * @param	guildID as String
	 * @param	cd as ChannelData
	 * @return	TRUE if CNL has been removed from GCNL or FALSE if not
	 */
	private boolean removeCNLfromGCNL(String guildID, ChannelData cd) {
		//check if GCNL exists before removing the CNL
		if(hasGCNL(guildID)) {
			return getGCNL(guildID).removeCNL(cd);
		}
		return false;
	}
	
	/**
	 * Checks if Discord server is listed
	 * @param	guildID as String
	 * @return	TRUE if Discord server is listed or FALSE if not
	 */
	public boolean hasGCNL(String guildID) {
		return getGCNLs().containsKey(guildID);
	}
	
	/**
	 * Checks if there are Discord server listed
	 * @return	TRUE if there are Discord server listed or FALSE if not
	 */
	public boolean isEmpty() {
		return getGCNLs().isEmpty();
	}

	/**
	 * Remove user from each notification list
	 * @param userID as String
	 */
	public void removeFromAllLists(String userID) {
		
		for(HashMap.Entry<String, GuildChannelNotificationList> gcnlEntry : getGCNLs().entrySet()) {
		    GuildChannelNotificationList gcnl = gcnlEntry.getValue();
		    removeFromAllListsOfGuild(gcnl.getID(), userID);
		}
	}
	
	
	public ArrayList<ChannelData> getNotificationChannelOfGuildForUser(String guildID, String userID) {
		ArrayList<ChannelData> channels = new ArrayList<ChannelData>();
		
		if(getGCNLs().containsKey(guildID)) {
			GuildChannelNotificationList gcnl = getGCNL(guildID);
			for(HashMap.Entry<String, ChannelNotificationList> cnlEntry : gcnl.getCNLs().entrySet()) {
			    ChannelNotificationList cnl = cnlEntry.getValue();
			    if(cnl.hasUser(userID)) {
			    	channels.add(new ChannelData(cnl.getID(), cnl.getName()));
			    }
			}
		}
		
		return channels;
	}
	
	/**
	 * Remove user from each notification list within a guild
	 * @param guildID as String
	 * @param userID as String
	 * @return
	 */
	public void removeFromAllListsOfGuild(String guildID, String userID) {
		if(getGCNLs().containsKey(guildID)) {
			GuildChannelNotificationList gcnl = getGCNL(guildID);
			for(HashMap.Entry<String, ChannelNotificationList> cnlEntry : gcnl.getCNLs().entrySet()) {
				ChannelNotificationList cnl = cnlEntry.getValue();
				removeNotification(new GuildData(gcnl.getID(), gcnl.getName()), new ChannelData(cnl.getID(), cnl.getName()), userID);
			    //cnl.removeUser(userID);
			    
			}
		}
	}
	
	/**
	 * Returns all userIDs for a channel
	 * @param gd as GuildData
	 * @param cd as ChannelData
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getUserIDsforChannel(GuildData gd, ChannelData cd){
		ArrayList<String> usrList = new ArrayList<String>();
		
		//check if guild has notification lists
		if(getGCNLs().containsKey(gd.getId())) {
			//check if there is a notification list for that channel
			if(getGCNL(gd.getId()).hasCNL(cd.getId())) {
				//TODO can be removed if lists will deleted after last user has been removed from list
				if(!getGCNL(gd.getId()).getCNL(cd.getId()).isEmpty()) {
					//add user to return list 
					for(String usr: getGCNL(gd.getId()).getCNL(cd.getId()).getUserList()) {
						usrList.add(usr);
					}
				}
			}
		}
		return usrList;
	}
	
	private void cleanLists() {
		//TODO Doesnt work atm and has to be fixed before referenced by persistData()
		for(HashMap.Entry<String, GuildChannelNotificationList> gcnlEntry : getGCNLs().entrySet()) {
			GuildChannelNotificationList gcnl = gcnlEntry.getValue();
			cleanGuildLists(gcnl.getID());
		}
	}
	
	private void cleanGuildLists(String guildID) {
		GuildChannelNotificationList gcnl = getGCNL(guildID);
		
		for(HashMap.Entry<String, ChannelNotificationList> cnlEntry : gcnl.getCNLs().entrySet()) {
			ChannelNotificationList cnl = cnlEntry.getValue();
			if(cnl.isEmpty()) {
				gcnl.removeCNL(new ChannelData(cnl.getID(), cnl.getName()));
			}
		}
		if(gcnl.isEmpty()) {
			removeGCNL(gcnl.getID());
		}
	}
	
	/**
	 * Persists data in JSON format
	 * @return	TRUE if data has been stored or FALSE if not
	 */
	public boolean persistData() {
		//cleanLists();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(new File(fileName), getGCNLs());
			return true;
			
		} catch (IOException e) {
			Antony.getLogger().error("Could not store GCNL data!", e);
		}
		return false;
	}
	
	/**
	 * Loads data in JSON format from stored file
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void initData() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		File f = new File(fileName);
		if(f.exists() && !f.isDirectory()) { 
			this.gcnl = objectMapper.readValue(new File(fileName), new TypeReference<Map<String, GuildChannelNotificationList>>(){});
		}
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public Map<String, GuildChannelNotificationList> getGCNLs() {
		return gcnl;
	}
}