package bot.antony;

import java.awt.Color;
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
import bot.antony.events.BlacklistNotification;
import bot.antony.events.CommandListener;
import bot.antony.events.GuildMemberJoin;
import bot.antony.events.GuildMemberLeave;
import bot.antony.events.GuildMemberUpdateNickname;
import bot.antony.events.GuildUpdateName;
import bot.antony.events.NotificationListener;
import bot.antony.events.OfferListener;
import bot.antony.events.ReactionAddEvent;
import bot.antony.events.UserUpdateName;
import bot.antony.events.UserUpdateOnlineStatus;
import bot.antony.events.WatchlistNotification;
import bot.antony.events.softban.SoftbanFilterListener;
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
	//private static DbController dbcontroller = new DbController();
	private static boolean prodStage = false;

	/**
	 * This is the method where the program starts.
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			// build bot
			JDA jda = JDABuilder.createDefault(getToken(isProdStage()))	// The token of the account that is logging in.
					.addEventListeners(new CommandListener())			// Listener for commands
					.addEventListeners(new NotificationListener())		// Listener for notification function
					.addEventListeners(new WatchlistNotification())		// listener which checks if posts contains blacklisted text strings
					.addEventListeners(new BlacklistNotification())
					.addEventListeners(new OfferListener())				// listener which checks if an offer in a specific channel has been posted
					.addEventListeners(new GuildMemberLeave())			// listener for leaving guild member
					.addEventListeners(new GuildMemberJoin())			// listener for joining guild member
					.addEventListeners(new GuildMemberUpdateNickname())
					.addEventListeners(new GuildUpdateName())
					.addEventListeners(new UserUpdateOnlineStatus())
					.addEventListeners(new UserUpdateName())
					.addEventListeners(new SoftbanFilterListener())
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
			

			
			// Initialize CDI context and its dependencies to database, ...
			// Custom initiallization is necessary due to standalone application
			//TODO: BROKEN!
			/*Weld weld = new Weld();
		    WeldContainer container = weld.initialize();
		    AntonyServiceInterface service = container.instance().select(AntonyServiceInterface.class).get();
		    service.init();
		    weld.shutdown();	//TODO: Move to antonys shutdown
		    */
			//AntonyServiceInterface service = AntonyService.getService();

			
			// Create log output after startup
			String stage = "PROD";
			if(!prodStage) {
				stage = "DEV/TEST";
			}
			String postStartLogEntry = "[" + stage + "] Antony (v" + getVersion() + ") started";
			logger.info(postStartLogEntry);
			
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
		}/* catch (SQLException e) {
			logger.error("Could not connect to database!", e);
			//e1.printStackTrace();
		}*/
		
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
