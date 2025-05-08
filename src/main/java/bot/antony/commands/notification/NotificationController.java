package bot.antony.commands.notification;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import bot.antony.Antony;
import bot.antony.guild.ChannelData;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

/**
 * NotificationController controls all notifications
 */
public class NotificationController {
	private Map<String, GuildChannelNotificationList> gcnls = new HashMap<String, GuildChannelNotificationList>();
	private ArrayList<UserNotification> pendingUserNotifications = new ArrayList<UserNotification>();
	private String notificationListConfigFileName;
	private String pendingNotificationsFileName;
	LocalDateTime nextUpdateDateTime;
	DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	private boolean waiting = false;

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public NotificationController() {
		this("antony.notifications.config.json", "antony.notifications.pending.json");
	}
	
	public NotificationController(String configFileName, String pendingFileName) {
		setNotificationListConfigFileName(configFileName);
		setPendingNotificationsFileName(pendingFileName);
		setNextUpdateDate(LocalDateTime.now().plusMinutes(Antony.getNotificationPendingTime()).truncatedTo(ChronoUnit.HOURS));
		initData();
		
		Antony.getLogger().info("Created notification controller. Next notification: " + nextUpdateDateTime.format(dtFormatter));
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
				//ArrayList<UserNotification> tempUserNotifications = new ArrayList<UserNotification>(getPendingUserNotifications());
				UserNotification userNotification = new UserNotification(user, guild);
				if(getPendingUserNotifications().contains(userNotification)) {
					for(UserNotification un : getPendingUserNotifications()) {
						if(un.equals(userNotification)) {
							un.getChannels().remove(channel);
						}
					}
				}
				
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
		UserNotification un = new UserNotification(user, guild);
		if(getPendingUserNotifications().contains(un)) {
			getPendingUserNotifications().remove(un);
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
	
	
	/*public void updateData() {
		for(HashMap.Entry<String, GuildChannelNotificationList> gcnlEntry : getGCNLs().entrySet()) {
			GuildChannelNotificationList gcnl = gcnlEntry.getValue();
			Guild guild = gcnl.getGuild().getId();
		}
	}*/

	public void checkPendingNotifications() {
		LocalDateTime now = LocalDateTime.now();
		if(getNextUpdateDate().isBefore(now)) {
			sendPendingNotifications();
			setNextUpdateDate(now.plusMinutes(Antony.getNotificationPendingTime()));
		}
	}

	/**
	 * will send all pending messages
	 */
	public void sendPendingNotifications() {
		for(UserNotification notification: getPendingUserNotifications()) {
			GuildData guildData = notification.getGuild();
			UserData userData = notification.getUser();
			ArrayList<ChannelData> channels = notification.getChannels();
			Guild guild = Antony.getJda().getGuildById(guildData.getId());

			//If user is a member of the guild
			if(guild.getMemberById(userData.getId()) != null && channels.size() > 0) {

				Member member = guild.getMemberById(userData.getId());
				StringBuilder logMessage = new StringBuilder();
				logMessage.append("On server [" + guildData.toString() + "] ");
				logMessage.append("user [" + userData.toString() + "] got pending notifications. ");
				logMessage.append("Notify about channels: ");
				int counter = 1;
				for(ChannelData channel: channels) {
					logMessage.append("[" + channel.toString() + "]");
					if(counter < channels.size()) {
						logMessage.append(", ");
						counter++;
					}
				}
				Antony.getLogger().info(logMessage.toString());

				EmbedBuilder eb = new EmbedBuilder().setTitle("Benachrichtigung über Kanal-Updates")
						.setColor(Antony.getBaseColor())
						.setThumbnail(guild.getIconUrl())
						.setDescription("Auf dem Server [" + guildData.getName() + "](https://discord.com/channels/" + guildData.getId() + ") "
								+ "gibt es Neuigkeiten in den von dir abonnierten Kanälen. Schau es dir gleich mal an!")
						.setFooter("Antony | Version " + Antony.getVersion());

				ArrayList<String> textList = new ArrayList<String>();
				StringBuilder fieldText = new StringBuilder();
				String textPart;
				int msgCounter = 1;

				for(ChannelData channel: channels) {
					textPart = "[#" + channel.getName() + "](https://discord.com/channels/" + guildData.getId() + "/" + channel.getId() + ")";
					if((fieldText.length() + textPart.length() + 2) > 1024) {
						textList.add(fieldText.toString());
						fieldText = new StringBuilder();
					}

					fieldText.append(textPart);
					if(msgCounter < channels.size()) {
						fieldText.append(", ");
						msgCounter++;
					} else {
						textList.add(fieldText.toString());
					}
				}

				msgCounter = 1;
				for(String text: textList) {
					if(msgCounter == textList.size()) {
						text += "\n_";
					}
					eb.addField("", text, false);
				}

				Utils.sendPM(member, eb);

			}
		}
		getPendingUserNotifications().clear();
		persistData();
	}

	
	/**
	 * Persists data in JSON format
	 * @return	TRUE if data has been stored or FALSE if not
	 */
	public boolean persistData() {
		while(isWaiting()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setWaiting(true);
		boolean retVal = false;
		if (Utils.saveJSONData(getNotificationListConfigFileName(), getGCNLs()) &&	Utils.saveJSONData(getPendingNotificationsFileName(), getPendingUserNotifications())) {
			retVal = true;
		}
		setWaiting(false);
		return retVal;
	}
	
	/**
	 * Loads data in JSON format from stored file
	 * @throws	JsonParseException
	 * @throws	JsonMappingException
	 * @throws	IOException
	 */
	@SuppressWarnings("unchecked")
	public void initData() {
		while(isWaiting()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setWaiting(true);
		this.gcnls = (Map<String, GuildChannelNotificationList>) Utils.loadJSONData(getNotificationListConfigFileName(), new TypeReference<Map<String, GuildChannelNotificationList>>(){}, this.gcnls);
		this.pendingUserNotifications = (ArrayList<UserNotification>) Utils.loadJSONData(getPendingNotificationsFileName(), new TypeReference<ArrayList<UserNotification>>(){}, this.pendingUserNotifications);
		setWaiting(false);
	}
	
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public Map<String, GuildChannelNotificationList> getGCNLs() {
		return gcnls;
	}
	
	public ArrayList<UserNotification> getPendingUserNotifications() {
		return pendingUserNotifications;
	}

	public void setPendingUserNotifications(ArrayList<UserNotification> pendingUserNotifications) {
		this.pendingUserNotifications = pendingUserNotifications;
	}

	public String getNotificationListConfigFileName() {
		return notificationListConfigFileName;
	}

	public void setNotificationListConfigFileName(String notificationListConfigFileName) {
		this.notificationListConfigFileName = notificationListConfigFileName;
	}

	public String getPendingNotificationsFileName() {
		return pendingNotificationsFileName;
	}

	public void setPendingNotificationsFileName(String pendingNotificationsFileName) {
		this.pendingNotificationsFileName = pendingNotificationsFileName;
	}
	
	public LocalDateTime getNextUpdateDate() {
		return nextUpdateDateTime;
	}

	public void setNextUpdateDate(LocalDateTime nextUpdateDateTime) {
		this.nextUpdateDateTime = nextUpdateDateTime;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}
	
}