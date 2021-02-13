package bot.antony;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import bot.antony.commands.notification.NotificationController;
import bot.antony.events.CommandListener;
import bot.antony.events.NotificationListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Antony extends ListenerAdapter {

	public static Antony INSTANCE;
	private static Color baseColor = new Color(31, 89, 152); // AAM blue
	private static String cmdPrefix;
	private static long notificationPendingTime;
	private static String version;
	private static Logger logger = LoggerFactory.getLogger(Antony.class);
	private static CommandManager cmdMan = new CommandManager();
	private static NotificationController notificationController = new NotificationController();
	private static boolean prodStage = false;

	/**
	 * This is the method where the program starts.
	 * @param args
	 */
	public static void main(String[] args) {
		
		setVersion(getProperty("bot.version"));														// set Antony version
		setCmdPrefix(getProperty("command.prefix"));												// set command prefix
		setNotificationPendingTime(Long.parseLong(getProperty("notification.pending.time"))*1000);	// set sleep time for sending notification thread

		try {
			// build bot
			JDA jda = JDABuilder.createDefault(getToken(isProdStage()))	// The token of the account that is logging in.
					.addEventListeners(new CommandListener())			// Listener for commands
					.addEventListeners(new NotificationListener())		// Listener for notification function
					.setChunkingFilter(ChunkingFilter.ALL)				// enable member chunking for all guilds
					.setMemberCachePolicy(MemberCachePolicy.ALL)		// ignored if chunking enabled
					.enableCache(CacheFlag.ACTIVITY)					// To get details on guild members
					.enableCache(CacheFlag.CLIENT_STATUS)				// To get client status
					.enableCache(CacheFlag.EMOTE)						// To get guilds emotes
					.enableIntents(GatewayIntent.GUILD_MEMBERS)			// Has to be set to use MemberCachePolicy.ALL
					.enableIntents(GatewayIntent.GUILD_PRESENCES)		// Has to be set to use CacheFlag.ACTIVITY
					.build();
			
			jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.
			jda.getPresence().setStatus(OnlineStatus.ONLINE); // Change bot status to online
			
			// Create log output after startup
			StringBuilder postStartLogEntry = new StringBuilder();
			postStartLogEntry.append("[");
			if(prodStage) {
				postStartLogEntry.append("PROD");
			} else {
				postStartLogEntry.append("DEV/TEST");
			}
			postStartLogEntry.append("] ");
			postStartLogEntry.append("Antony (v" + getVersion() + ") started");
			logger.info(postStartLogEntry.toString());
			notificationController.initData();
			
			Thread sendPendingNotifications = new Thread() {
				public void run() {
					while(jda.getPresence().getStatus() == OnlineStatus.ONLINE) {
						try {
							
							//System.out.println(counter + " Thread Running");
							notificationController.sendPendingNotifications(jda);
							Thread.sleep(getNotificationPendingTime());
						} catch (InterruptedException e) {
							logger.error("Wasn't able to put Thread asleep.", e);
						}
					}
				}
			};
			sendPendingNotifications.start();
			
			
		} catch (LoginException e) {
			logger.error("Could not login to Discord!", e);
		} catch (InterruptedException e) {
			logger.error("Antony thread is interrupted while waiting!", e);
		} catch (JsonParseException | JsonMappingException e) {
			logger.error("Could not parse GCNL data!", e);
		} catch (IOException e) {
			logger.error("Could not read GCNL file!", e);
		}
		
	}


	/**
	 * Function to get value for key from antony.properties file
	 * @param	key
	 * 			as String
	 * @return	value
	 * 			as String
	 */
	private static String getProperty(String key) {
		InputStream is = Antony.class.getResourceAsStream("/antony.properties");
		Properties prop = new Properties();
		try {
			prop.load(is);
			return prop.getProperty(key);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Function to get bot token
	 * @param	prod
	 * 			Boolean if bot is for productive use
	 * @return	Discord Bot Token
	 * 			as String
	 */
	private static String getToken(boolean prod) {
		if(prod) {
			return getProperty("bot.token.prod");
		}
		return getProperty("bot.token.dev");
	}
	
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	
	/**
	 * Function to get Antonys base color
	 * @return	baseColor
	 * 			as Color
	 */
	public static Color getBaseColor() {
		return baseColor;
	}

	/**
	 * Function to set Antonys base color
	 * @param	baseColor
	 * 			as Color
	 */
	public static void setBaseColor(Color baseColor) {
		Antony.baseColor = baseColor;
	}
	
	/**
	 * Function to get Antonys version
	 * @return	program version
	 */
	public static String getVersion() {
		return version;
	}

	/**
	 * Function to set Antonys version
	 * @param	version
	 * 			as String
	 */
	public static void setVersion(String version) {
		Antony.version = version;
	}

	/**
	 * Function to get the CommandManager
	 * @return	CommandManager
	 */
	public static CommandManager getCmdMan() {
		return cmdMan;
	}
	
	/**
	 * Function to get the NotificationManager
	 * @return NotificationManager
	 */
	public static NotificationController getNotificationController() {
		return notificationController;
	}

	/**
	 * Function to check if Bot is used productive
	 * @return	prodStage
	 * 			as Boolean
	 */
	public static boolean isProdStage() {
		return prodStage;
	}
	
	/**
	 * Function to get Logger
	 * @return	Logger
	 */
	public static Logger getLogger() {
		return logger;
	}
	
	/**
	 * Function to get command prefix
	 * @return	cmdPrefix
	 * 			as String
	 */
	public static String getCmdPrefix() {
		return cmdPrefix;
	}

	/**
	 * Function to set command prefix
	 * @param	cmdPrefix
	 * 			as String
	 */
	public static void setCmdPrefix(String cmdPrefix) {
		Antony.cmdPrefix = cmdPrefix;
	}


	public static long getNotificationPendingTime() {
		return notificationPendingTime;
	}


	public static void setNotificationPendingTime(long notificationPendingTime) {
		Antony.notificationPendingTime = notificationPendingTime;
	}
}
