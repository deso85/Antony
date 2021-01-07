package bot.antony.notifications;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bot.antony.Antony;

/**
 * ChannelNotificationList stores all user which have to be notified on updates for a specific channel
 */
public class ChannelNotificationList {
	private ChannelData cd = new ChannelData();
	private ArrayList<String> userList = new ArrayList<String>();

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ChannelNotificationList() {
		super();
	}
	
	public ChannelNotificationList(ChannelData cd) {
		this.cd = cd;
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	/**
	 * Adds a user to the CNL
	 * @param	userID as String
	 * @return	TRUE if user has been added or FALSE if user already was on the list
	 */
	public boolean addUser(String userID) {
		if(!getUserList().contains(userID)) {
			getUserList().add(userID);
			return true;
		}
		return false;
	}
	
	/**
	 * Removes a user from the CNL
	 * @param	userID as String
	 * @return	TRUE if user has been removed or FALSE if not
	 */
	public boolean removeUser(String userID) {
		if(getUserList().contains(userID)) {
			getUserList().remove(userID);
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if user is on the CNL
	 * @param	userID as String
	 * @return	TRUE if on the CNL or FALSE if not
	 */
	public boolean hasUser(String userID) {
		return getUserList().contains(userID);
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
			StringBuilder sb = new StringBuilder();
			sb.append("User in CNL " + getID() + " (" + getName() + "): " );
			int counter = 1;
			for(String user: getUserList()) {
				sb.append(user);
				if(counter < getUserList().size()) {
					sb.append(", ");
				}
				counter++;
			}
			Antony.getLogger().info(sb.toString());
		} else {
			Antony.getLogger().info("CNL " + getID() + " (" + getName() + ") is empty.");
		}
	}
	
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public String getID() {
		return this.cd.getId();
	}

	public void setID(String id) {
		this.cd.setId(id);
	}
	
	public String getName() {
		return this.cd.getName();
	}

	public void setName(String name) {
		this.cd.setName(name);
	}
	
	public ArrayList<String> getUserList() {
		return userList;
	}
}