package bot.antony.commands.notification;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bot.antony.Antony;
import bot.antony.guild.ChannelData;
import bot.antony.guild.UserData;

/**
 * ChannelNotificationList stores all user which have to be notified on updates for a specific channel
 */
public class ChannelNotificationList {
	private ChannelData channel = new ChannelData();
	private ArrayList<UserData> userList = new ArrayList<UserData>();

	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ChannelNotificationList() {
		super();
	}
	
	public ChannelNotificationList(ChannelData channel) {
		this.channel = channel;
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	/**
	 * Adds a user to the CNL
	 * @param	userID as String
	 * @return	TRUE if user has been added or FALSE if user already was on the list
	 */
	public boolean addUser(UserData user) {
		if(!hasUser(user)) {
			return getUserList().add(user);
		}
		return false;
	}
	
	/**
	 * Removes a user from the CNL
	 * @param	user as UserData
	 * @return	TRUE if user has been removed or FALSE if not
	 */
	public boolean removeUser(UserData user) {
		if(hasUser(user)) {
			return getUserList().remove(user);
		}
		return false;
	}
	
	/**
	 * Checks if user is listed on CNL
	 * @param	user as UserData
	 * @return	TRUE if on the CNL or FALSE if not
	 */
	public boolean hasUser(UserData user) {
		return getUserList().contains(user);
	}
	
	/**
	 * Checks if CNL is empty
	 * @return	TRUE if list is empty or FALSE if not
	 */
	@JsonIgnore
	public boolean isEmpty() {
		return getUserList().isEmpty();
	}
	
	/**
	 * Clears user list
	 */
	public void clear() {
		getUserList().clear();
	}
	
	/**
	 * Prints all user which will be notified on updates
	 */
	public void printUser() {
		if(!isEmpty()) {
			StringBuilder logEntry = new StringBuilder();
			logEntry.append("User in CNL [" + toString() + "]: " );
			int counter = 1;
			for(UserData user: getUserList()) {
				logEntry.append("[" + user.toString() + "]");
				if(counter < getUserList().size()) {
					logEntry.append(", ");
					counter++;
				}
			}
			Antony.getLogger().info(logEntry.toString());
		} else {
			Antony.getLogger().info("CNL [" + toString() + "] is empty.");
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
		return this.channel.getId();
	}

	@JsonIgnore
	public void setId(String id) {
		this.channel.setId(id);
	}
	
	@JsonIgnore
	public String getName() {
		return this.channel.getName();
	}

	@JsonIgnore
	public void setName(String name) {
		this.channel.setName(name);
	}
	
	public ChannelData getChannel() {
		return this.channel;
	}
	
	public ArrayList<UserData> getUserList() {
		return userList;
	}
}