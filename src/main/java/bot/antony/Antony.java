package bot.antony;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.antony.commands.notification.NotificationController;
import bot.antony.controller.BlackListController;
import bot.antony.controller.GuildController;
import bot.antony.controller.SoftbanController;
import bot.antony.controller.UserController;
import bot.antony.controller.WatchListController;
import bot.antony.controller.WhiteListController;
import bot.antony.events.CommandListener;
import bot.antony.events.GuildMemberJoin;
import bot.antony.events.GuildMemberLeave;
import bot.antony.events.GuildMemberUpdateNickname;
import bot.antony.events.GuildUpdateName;
import bot.antony.events.GuildVoiceJoin;
import bot.antony.events.GuildVoiceLeave;
import bot.antony.events.GuildVoiceMove;
import bot.antony.events.MessageReceived;
import bot.antony.events.MessageUpdate;
import bot.antony.events.NotificationListener;
import bot.antony.events.OfferListener;
import bot.antony.events.ReactionAddEvent;
import bot.antony.events.UserUpdateName;
import bot.antony.events.UserUpdateOnlineStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Antony extends ListenerAdapter {

	public static Antony INSTANCE;
	private static Color baseColor = new Color(31, 89, 152);
	private static String cmdPrefix = getProperty("command.prefix");
	private static long notificationPendingTime = Long.parseLong(getProperty("notification.pending.time"));
	private static String version = getProperty("bot.version");
	private static String dataPath = getProperty("flatfile.path");
	private static Logger logger = LoggerFactory.getLogger(Antony.class);
	private static CommandManager cmdMan = new CommandManager();
	private static NotificationController notificationController = new NotificationController();
	private static WhiteListController whitelistController = new WhiteListController();
	private static WatchListController watchlistController = new WatchListController();
	private static BlackListController blacklistController = new BlackListController();
	private static SoftbanController softbanController = new SoftbanController();
	private static GuildController guildController = new GuildController();
	private static UserController userController = new UserController();
	private static int usercount = 0;
	private static String configFile = null;

	/**
	 * This is the method where the program starts.
	 * @param args
	 */
	public static void main(String[] args) {

		//Set config file to owerwrite settings
		for(int i = 0; i < args.length; i++) {
            if(args[i].startsWith("-config=")) {
            	File cFile = new File(args[i].substring(8));
            	if(cFile.exists() && !cFile.isDirectory()) { 
            		setConfigFile(args[i].substring(8));
            	}
            }
        }
		
		try {
			// build bot
			JDA jda = JDABuilder.createDefault(getProperty("bot.token"))	// The token of the account that is logging in.
					.addEventListeners(new MessageReceived())
					.addEventListeners(new MessageUpdate())
					.addEventListeners(new CommandListener())			// Listener for commands
					.addEventListeners(new GuildVoiceJoin())
					.addEventListeners(new GuildVoiceMove())
					.addEventListeners(new GuildVoiceLeave())
					.addEventListeners(new NotificationListener())		// Listener for notification function
					.addEventListeners(new OfferListener())				// listener which checks if an offer in a specific channel has been posted
					.addEventListeners(new GuildMemberLeave())			// listener for leaving guild member
					.addEventListeners(new GuildMemberJoin())			// listener for joining guild member
					.addEventListeners(new GuildMemberUpdateNickname())
					.addEventListeners(new GuildUpdateName())
					.addEventListeners(new UserUpdateOnlineStatus())
					.addEventListeners(new UserUpdateName())
					.addEventListeners(new ReactionAddEvent())
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
			
			// Set status with command to see command list and some basic bot information
			for(Guild guild: jda.getGuilds()) {
				usercount += guild.getMemberCount();
			}
			jda.getPresence().setActivity(Activity.listening(cmdPrefix + "antony | " + usercount + " User | " + jda.getGuilds().size() + " Server"));
			

			// Create log output after startup
			StringBuilder postStartLogEntry = new StringBuilder();
			if(getProperty("bot.stage") != null) {
				postStartLogEntry.append("[" + getProperty("bot.stage") + "] ");
			}
			postStartLogEntry.append("Antony (v" + getVersion() + ") started");
			logger.info(postStartLogEntry.toString());
			
			//Thread which is used to send channel notifications 
			Thread sendPendingNotifications = new Thread() {
				public void run() {
					while(jda.getPresence().getStatus() == OnlineStatus.ONLINE) {
						try {
							//System.out.println(counter + " Thread Running");
							notificationController.sendPendingNotifications(jda);
							Thread.sleep(60000);	//60sec
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
		}
		
	}

	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------

	/**
	 * Function to get value for key from antony.properties file
	 * @param	key
	 * 			as String
	 * @return	value
	 * 			as String
	 */
	public static String getProperty(String key) {
		InputStream is = null;
		Properties prop = new Properties();
		String retVal = null;
		
		if(configFile != null && !configFile.equals("")) {
			try {
				is = new FileInputStream(configFile);
				prop.load(is);
				if(prop.getProperty(key) != null) {
					retVal = prop.getProperty(key);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(retVal == null){
			try {
				is = Antony.class.getResourceAsStream("/antony.properties");
				prop.load(is);
				retVal = prop.getProperty(key);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return retVal;
	}
	
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public static void setConfigFile(String configFile) {
		Antony.configFile = configFile;
	}
	
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
		//TODO: Remove
		return true;
	}
	
	public static String getDataPath() {
		return dataPath;
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


	public static WhiteListController getWhitelistController() {
		return whitelistController;
	}
	
	public static WatchListController getWatchlistController() {
		return watchlistController;
	}
	
	public static BlackListController getBlacklistController() {
		return blacklistController;
	}
	
	public static GuildController getGuildController() {
		return guildController;
	}


	public static UserController getUserController() {
		return userController;
	}

	public static int getUsercount() {
		return usercount;
	}


	public static void setUsercount(int usercount) {
		Antony.usercount = usercount;
	}


	public static SoftbanController getSoftbanController() {
		return softbanController;
	}
	
	
}
