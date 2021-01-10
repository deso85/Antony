package bot.antony.commands.notification;

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
import bot.antony.guild.GuildData;
import bot.antony.guild.channel.ChannelData;
import bot.antony.guild.user.UserData;

/**
 * NotificationController controls all notifications
 */
public class NotificationController {
	private Map<String, GuildChannelNotificationList> gcnls = new HashMap<String, GuildChannelNotificationList>();
	private String fileName;
	
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public NotificationController() {
		setFileName("antony.notifications.json");
	}
	
	public NotificationController(String filename) {
		setFileName(filename);
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	/**
	 * Toggles user notification for channel
	 * @param	guild as GuildData
	 * @param	channel as ChannelData
	 * @param	user as UserData
	 * @return	TRUE if user has been added to notification or FALSE if user has been removed from notifications
	 */
	public boolean toggleNotification(GuildData guild, ChannelData channel, UserData user) {
		//If return value of addNotification is FALSE the user already listed and has to be removed
		if(addNotification(guild, channel, user)) {
			return true;
		}
		removeNotification(guild, channel, user);
		return false;
		
	}
	
	/**
	 * Adds user to notification list if necessary
	 * @param	guild as GuildData
	 * @param	channel as ChannelData
	 * @param	user as UserData
	 * @return	TRUE if user has been added or FALSE if not
	 */
	public boolean addNotification(GuildData guild, ChannelData channel, UserData user) {
		//functions will take care to not add duplicates
		addGCNL(guild);
		addCNLtoGCNL(guild, channel);
		return getGCNL(guild).getCNL(channel).addUser(user);
	}
	
	/**
	 * Removes user from notification list if possible
	 * @param	guild as GuildData
	 * @param	channel as ChanelData
	 * @param	user as UserData
	 * @return	TRUE if user has been removed from notification List or FALSE if not
	 */
	public boolean removeNotification(GuildData guild, ChannelData channel, UserData user) {
		//if GCNL exists
		if(hasGCNL(guild)) {
			//if GCNL has CNL
			if(getGCNL(guild).hasCNL(channel)) {
				//remove user if possible
				return getGCNL(guild).getCNL(channel).removeUser(user);
			}
		}
		return false;
	}
	
	/**
	 * Adds GCNL if necessary
	 * @param	guild as GuildData
	 * @return	TRUE if GCNL was added or FALSE if GCNL already was listed
	 */
	private boolean addGCNL(GuildData guild) {
		//check if GCNL isn't already listed
		if(!hasGCNL(guild)) {
			getGCNLs().put(guild.getId(), new GuildChannelNotificationList(guild));
			return true;
		}
		return false;
	}
	
	/**
	 * Returns GCNL if listed
	 * @param	guild as GuildData
	 * @return	GuildChannelNotificationList
	 */
	public GuildChannelNotificationList getGCNL(GuildData guild) {
		//check if GCNL exists before returning it
		if(hasGCNL(guild)) {
			return getGCNLs().get(guild.getId());
		}
		return null;
	}
	
	/**
	 * Removes GCNL if possible
	 * @param	guild as GuildData
	 * @return	TRUE if GCNL has been removed or FALSE if not
	 */
	private boolean removeGCNL(GuildData guild) {
		//check if GCNL exists before clearing and removing it
		if(hasGCNL(guild)) {
			getGCNLs().remove(guild.getId());
			return true;
		}
		return false;
	}
	
	/**
	 * Adds a CNL to a GCNL
	 * @param	guild as GuildData
	 * @param	channel as ChannelData
	 * @return	TRUE if CNL has been added or FALSE if not
	 */
	private boolean addCNLtoGCNL(GuildData guild, ChannelData channel) {
		//check if GCNL exists before adding the CNL
		if(hasGCNL(guild)) {
			return getGCNL(guild).addCNL(channel);
		}
		return false;
	}
	
	/**
	 * Removes CNL from GCNL if possible
	 * @param	guild as GuildData
	 * @param	channel as ChannelData
	 * @return	TRUE if CNL has been removed from GCNL or FALSE if not
	 */
	private boolean removeCNLfromGCNL(GuildData guild, ChannelData channel) {
		//check if GCNL exists before removing the CNL
		if(hasGCNL(guild)) {
			return getGCNL(guild).removeCNL(channel);
		}
		return false;
	}
	
	/**
	 * Checks if Discord server is listed
	 * @param	guild as GuildData
	 * @return	TRUE if Discord server is listed or FALSE if not
	 */
	public boolean hasGCNL(GuildData guild) {
		return getGCNLs().containsKey(guild.getId());
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
	 * @param	user as UserData
	 */
	public void removeUserFromAllLists(UserData user) {
		for(HashMap.Entry<String, GuildChannelNotificationList> gcnlEntry : getGCNLs().entrySet()) {
		    GuildChannelNotificationList gcnl = gcnlEntry.getValue();
		    removeUserFromAllListsOfGuild(gcnl.getGuild(), user);
		}
	}
	
	/**
	 * Get all channels of a guild the user gets notifications for
	 * @param	guild as GuildData
	 * @param	user as UserData
	 * @return	ArrayList<ChannelData>
	 */
	public ArrayList<ChannelData> getNotificationChannelOfGuildForUser(GuildData guild, UserData user) {
		ArrayList<ChannelData> channels = new ArrayList<ChannelData>();
		
		if(getGCNLs().containsKey(guild.getId())) {
			GuildChannelNotificationList gcnl = getGCNL(guild);
			for(HashMap.Entry<String, ChannelNotificationList> cnlEntry : gcnl.getCNLs().entrySet()) {
			    ChannelNotificationList cnl = cnlEntry.getValue();
			    if(cnl.hasUser(user)) {
			    	channels.add(cnl.getChannel());
			    }
			}
		}
		return channels;
	}
	
	/**
	 * Remove user from every notification list within a guild
	 * @param	guild as GuildData
	 * @param	user as UserData
	 */
	public void removeUserFromAllListsOfGuild(GuildData guild, UserData user) {
		if(getGCNLs().containsKey(guild.getId())) {
			GuildChannelNotificationList gcnl = getGCNL(guild);
			for(HashMap.Entry<String, ChannelNotificationList> cnlEntry : gcnl.getCNLs().entrySet()) {
				ChannelNotificationList cnl = cnlEntry.getValue();
				removeNotification(gcnl.getGuild(), cnl.getChannel(), user);
			}
		}
	}
	
	/**
	 * Returns all user for a channel
	 * @param	guild as GuildData
	 * @param	channel as ChannelData
	 * @return	ArrayList<UserData>
	 */
	public ArrayList<UserData> getChannelUser(GuildData guild, ChannelData channel){
		ArrayList<UserData> usrList = new ArrayList<UserData>();
		
		//check if guild has notification lists
		if(getGCNLs().containsKey(guild.getId())) {
			//check if there is a notification list for that channel
			if(getGCNL(guild).hasCNL(channel)) {
				//TODO can be removed if lists will deleted after last user has been removed from list
				if(!getGCNL(guild).getCNL(channel).isEmpty()) {
					//add user to return list 
					for(UserData user: getGCNL(guild).getCNL(channel).getUserList()) {
						usrList.add(user);
					}
				}
			}
		}
		return usrList;
	}
	
	/**
	 * Cleans all lists so that there are no empty ones
	 */
	private void cleanLists() {
		//TODO Doesn't work atm and has to be fixed before referenced by persistData()
		for(HashMap.Entry<String, GuildChannelNotificationList> gcnlEntry : getGCNLs().entrySet()) {
			GuildChannelNotificationList gcnl = gcnlEntry.getValue();
			cleanGuildLists(gcnl.getGuild());
		}
	}
	
	/**
	 * Cleans all guild specific lists so that there are no empty ones
	 * @param	guild as GuildData
	 */
	private void cleanGuildLists(GuildData guild) {
		//TODO Doesn't work atm
		GuildChannelNotificationList gcnl = getGCNL(guild);
		
		for(HashMap.Entry<String, ChannelNotificationList> cnlEntry : gcnl.getCNLs().entrySet()) {
			ChannelNotificationList cnl = cnlEntry.getValue();
			if(cnl.isEmpty()) {
				gcnl.removeCNL(cnl.getChannel());
			}
		}
		if(gcnl.isEmpty()) {
			removeGCNL(gcnl.getGuild());
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
			objectMapper.writeValue(new File(getFileName()), getGCNLs());
			return true;
			
		} catch (IOException e) {
			Antony.getLogger().error("Could not store GCNL data!", e);
		}
		return false;
	}
	
	/**
	 * Loads data in JSON format from stored file
	 * @throws	JsonParseException
	 * @throws	JsonMappingException
	 * @throws	IOException
	 */
	public void initData() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		File file = new File(getFileName());
		if(file.exists() && !file.isDirectory()) { 
			this.gcnls = objectMapper.readValue(new File(getFileName()), new TypeReference<Map<String, GuildChannelNotificationList>>(){});
		}
	}
	
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public Map<String, GuildChannelNotificationList> getGCNLs() {
		return gcnls;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}