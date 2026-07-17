package bot.antony;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.antony.commands.aam.events.OfferListener;
import bot.antony.commands.aam.events.ProposalListener;
import bot.antony.commands.antcheck.AntcheckNotificationController;
import bot.antony.commands.antcheck.AntcheckController;
import bot.antony.commands.notification.NotificationController;
import bot.antony.controller.AAMHBController;
import bot.antony.controller.BlackListController;
import bot.antony.controller.GiveawayController;
import bot.antony.controller.GuildController;
import bot.antony.controller.ReminderController;
import bot.antony.controller.SoftbanController;
import bot.antony.controller.UserController;
import bot.antony.controller.WatchListController;
import bot.antony.controller.WhiteListController;
import bot.antony.events.CommandListener;
import bot.antony.events.GuildMemberJoin;
import bot.antony.events.GuildMemberLeave;
import bot.antony.events.GuildMemberUpdateNickname;
import bot.antony.events.GuildUpdateName;
import bot.antony.events.GuildVoiceUpdate;
import bot.antony.events.MessageReceived;
import bot.antony.events.MessageUpdate;
import bot.antony.events.NotificationListener;
import bot.antony.events.ReactionAddEvent;
import bot.antony.events.ThreadCreate;
import bot.antony.events.ThreadDelete;
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

/**
 * Main class for the Antony Discord bot.
 * 
 * Antony is a feature-rich Discord bot that handles various guild management,
 * user interactions, notifications, giveaways, reminders, and more.
 * This class initializes the bot, manages all controllers and listeners,
 * and provides access to the JDA instance and bot configuration.
 */
public class Antony extends ListenerAdapter {

	/** Singleton instance of Antony bot */
	public static Antony INSTANCE;
	/** Base color for bot embeds and messages */
	private static Color baseColor = new Color(31, 89, 152);
	/** Logger instance for logging bot events and errors */
	private static Logger logger = LoggerFactory.getLogger(Antony.class);
	/** Command prefix for invoking bot commands (e.g., "!" or "?") */
	private static String cmdPrefix;
	/** Time in milliseconds to wait for notification processing */
	private static long notificationPendingTime;
	/** Current version of the Antony bot */
	private static String version = getProperty("bot.version");
	/** Path to the data directory for flatfile storage */
	private static String dataPath;
	/** Manager for command execution and routing */
	private static CommandManager cmdMan;
	/** Manager for reaction-based interactions */
	private static ReactionManager reactionMan;
	/** Controller for antcheck functionality */
	private static AntcheckController antcheckController;
	/** Controller for antcheck notifications */
	private static AntcheckNotificationController antcheckNotificationController;
	/** Controller for notifying members about new channel entries */
	private static NotificationController notificationController;
	/** Controller for whitelist management */
	private static WhiteListController whitelistController;
	/** Controller for watchlist management */
	private static WatchListController watchlistController;
	/** Controller for blacklist management */
	private static BlackListController blacklistController;
	/** Controller for softban functionality */
	private static SoftbanController softbanController;
	/** Controller for guild-related operations */
	private static GuildController guildController;
	/** Controller for user-related operations */
	private static UserController userController;
	/** Controller for AAM/HB (Keepers Journal) functionality */
	private static AAMHBController hbController;
	/** Controller for giveaway management */
	private static GiveawayController gaController;
	/** Controller for reminder management */
	private static ReminderController reminderController;
	/** Total count of users across all guilds */
	private static int usercount;
	/** Path to the custom configuration file (if provided) */
	private static String configFile = null;
	/** JDA instance representing the bot's connection to Discord */
	private static JDA jda;
	/** List of timer threads for background tasks and periodic checks */
	private static final List<Thread> timerThreads = new ArrayList<>();
	/**
	 * This is the method where the program starts.
	 * @param args
	 */
	public static void main(String[] args) {
		initializeConfig(args);
		initializeControllers();
		startBot();
	}

	/**
	 * Parse a configuration file path argument from command-line arguments.
	 * Expects format: -config=/path/to/config.properties
	 * 
	 * @param argument the command-line argument to parse
	 * @return Optional containing the config path if valid, empty otherwise
	 */
	private static Optional<String> parseConfigArgument(String argument) {
		if (argument == null || !argument.startsWith("-config=")) {
			return Optional.empty();
		}

		String configPath = argument.substring("-config=".length()).trim();
		return configPath.isEmpty() ? Optional.empty() : Optional.of(configPath);
	}

	/**
	 * Initialize bot configuration from command-line arguments and property files.
	 * Loads command prefix, notification pending time, and data path from properties.
	 * 
	 * @param args command-line arguments (may contain -config=/path/to/config)
	 */
	private static void initializeConfig(String[] args) {
		Arrays.stream(args)
				.map(Antony::parseConfigArgument)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.ifPresent(Antony::setConfigFile);

		cmdPrefix = getProperty("command.prefix");
		notificationPendingTime = Long.parseLong(getProperty("notification.pending.time"));
		dataPath = getProperty("flatfile.path");
	}

	/**
	 * Initialize all bot controllers responsible for managing features and data.
	 * Each controller handles a specific bot feature (commands, reactions, notifications, etc.).
	 */
	private static void initializeControllers() {
		cmdMan = new CommandManager();
		reactionMan = new ReactionManager();
		antcheckController = new AntcheckController();
		antcheckNotificationController = new AntcheckNotificationController();
		notificationController = new NotificationController();
		whitelistController = new WhiteListController();
		watchlistController = new WatchListController();
		blacklistController = new BlackListController();
		softbanController = new SoftbanController();
		guildController = new GuildController();
		userController = new UserController();
		hbController = new AAMHBController();
		gaController = new GiveawayController();
		reminderController = new ReminderController();
		usercount = 0;
	}

	/**
	 * Build and start the Discord bot.
	 * 
	 * Configures JDA with all event listeners, cache settings, and gateway intents.
	 * Sets up the bot's presence, initializes all background controllers and timers,
	 * and starts the notification and giveaway processing loops.
	 * 
	 * @throws InterruptedException if the bot thread is interrupted during startup
	 */
	private static void startBot() {
		try {
			// build bot
			jda = JDABuilder.createDefault(getProperty("bot.token"))	// The token of the account that is logging in.
					.addEventListeners(new MessageReceived())
					.addEventListeners(new MessageUpdate())
					.addEventListeners(new CommandListener())			// Listener for commands
					.addEventListeners(new GuildVoiceUpdate())
					.addEventListeners(new NotificationListener())		// Listener for notification function
					.addEventListeners(new OfferListener())				// listener which checks if an offer in a specific channel has been posted
					.addEventListeners(new ProposalListener())
					.addEventListeners(new GuildMemberLeave())			// listener for leaving guild member
					.addEventListeners(new GuildMemberJoin())			// listener for joining guild member
					.addEventListeners(new GuildMemberUpdateNickname())
					.addEventListeners(new GuildUpdateName())
					.addEventListeners(new UserUpdateOnlineStatus())
					.addEventListeners(new UserUpdateName())
					.addEventListeners(new ReactionAddEvent())
					.addEventListeners(new ThreadCreate())
					.addEventListeners(new ThreadDelete())
					.setChunkingFilter(ChunkingFilter.ALL)				// enable member chunking for all guilds
					.setMemberCachePolicy(MemberCachePolicy.ALL)		// ignored if chunking enabled
					.enableCache(CacheFlag.ACTIVITY)					// To get details on guild members
					.enableCache(CacheFlag.CLIENT_STATUS)				// To get client status
					.enableCache(CacheFlag.EMOJI)						// To get guilds emotes
					.enableIntents(GatewayIntent.GUILD_MEMBERS)			// Has to be set to use MemberCachePolicy.ALL
					.enableIntents(GatewayIntent.GUILD_PRESENCES)		// Has to be set to use CacheFlag.ACTIVITY
					.enableIntents(GatewayIntent.MESSAGE_CONTENT)
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
			
			hbController.setVars(jda);
			gaController.load();
			gaController.run(jda);
			reminderController.load();
			reminderController.run(jda);
			antcheckController.run(jda);
			antcheckNotificationController.run();
			//Thread which is used to do timed actions
			Thread timerThread = new Thread() {
				public void run() {
					while(jda.getPresence().getStatus() == OnlineStatus.ONLINE) {
						try {
							notificationController.checkPendingNotifications();
							hbController.checkHBs();
							Thread.sleep(60000);	//60sec
						} catch (InterruptedException e) {
							logger.info("Timer thread interrupted (likely during restart).");
							Thread.currentThread().interrupt(); // restore interrupted status
							break;
						}
					}
				}
			};
			timerThread.setName("antony-timer");
			timerThreads.add(timerThread);
			timerThread.start();
			
			
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
		Properties prop = new Properties();
		String retVal = null;
		
		if(configFile != null && !configFile.isEmpty()) {
			try (InputStream is = new FileInputStream(configFile)) {
				prop.load(is);
				retVal = prop.getProperty(key);
			} catch (IOException e) {
				if (logger != null) {
					logger.warn("Unable to load config file {}", configFile, e);
				}
			}
		}

		if(retVal == null){
			try (InputStream is = Antony.class.getResourceAsStream("/antony.properties")) {
				if (is != null) {
					prop.load(is);
					retVal = prop.getProperty(key);
				}
			} catch (IOException e) {
				if (logger != null) {
					logger.warn("Unable to load default properties", e);
				}
			}
		}
		
		if(retVal == null){
			try (InputStream is = Antony.class.getResourceAsStream("/antony.properties.tpl")) {
				if (is != null) {
					prop.load(is);
					retVal = prop.getProperty(key);
				}
			} catch (IOException e) {
				if (logger != null) {
					logger.warn("Unable to load template properties", e);
				}
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
	 * Function to get the ReactionManager
	 * @return	ReactionManager
	 */
	public static ReactionManager getReactionMan() {
		return reactionMan;
	}
	
	/**
	 * Function to get the AntcheckController
	 * @return AntcheckController instance
	 */
	public static AntcheckController getAntcheckController() {
		return antcheckController;
	}
	
	/**
	 * Function to get the AntcheckNotificationController
	 * @return AntcheckNotificationController instance
	 */
	public static AntcheckNotificationController getAntcheckNotificationController() {
		return antcheckNotificationController;
	}
	
	/**
	 * Function to get the NotificationController (notifies members about new channel entries)
	 * @return NotificationController instance
	 */
	public static NotificationController getNotificationController() {
		return notificationController;
	}
	
	/**
	 * Function to get the data directory path
	 * @return dataPath as String
	 */
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

	/**
	 * Function to get the notification pending time
	 * @return notificationPendingTime in milliseconds
	 */
	public static long getNotificationPendingTime() {
		return notificationPendingTime;
	}

	/**
	 * Function to set the notification pending time
	 * @param notificationPendingTime in milliseconds
	 */
	public static void setNotificationPendingTime(long notificationPendingTime) {
		Antony.notificationPendingTime = notificationPendingTime;
	}

	/**
	 * Function to get the WhiteListController
	 * @return WhiteListController instance
	 */
	public static WhiteListController getWhitelistController() {
		return whitelistController;
	}
	
	/**
	 * Function to get the WatchListController
	 * @return WatchListController instance
	 */
	public static WatchListController getWatchlistController() {
		return watchlistController;
	}
	
	/**
	 * Function to get the BlackListController
	 * @return BlackListController instance
	 */
	public static BlackListController getBlacklistController() {
		return blacklistController;
	}
	
	/**
	 * Function to get the GuildController
	 * @return GuildController instance
	 */
	public static GuildController getGuildController() {
		return guildController;
	}

	/**
	 * Function to get the UserController
	 * @return UserController instance
	 */
	public static UserController getUserController() {
		return userController;
	}
	
	/**
	 * Function to get the AAMHBController (Keepers Journal Controller)
	 * @return AAMHBController instance
	 */
	public static AAMHBController getHBController() {
		return hbController;
	}
	
	/**
	 * Function to get the GiveawayController
	 * @return GiveawayController instance
	 */
	public static GiveawayController getGiveawayController() {
		return gaController;
	}
	
	/**
	 * Function to get the ReminderController
	 * @return ReminderController instance
	 */
	public static ReminderController getReminderController() {
		return reminderController;
	}
	
	/**
	 * Function to get the total user count across all guilds
	 * @return usercount as integer
	 */
	public static int getUsercount() {
		return usercount;
	}

	/**
	 * Function to set the total user count
	 * @param usercount as integer
	 */
	public static void setUsercount(int usercount) {
		Antony.usercount = usercount;
	}

	/**
	 * Function to get the SoftbanController
	 * @return SoftbanController instance
	 */
	public static SoftbanController getSoftbanController() {
		return softbanController;
	}

	/**
	 * Function to get the JDA instance
	 * @return JDA instance representing the bot's Discord connection
	 */
	public static JDA getJda() {
		return jda;
	}

	/**
	 * Interrupt all timer threads so they wake up from sleep immediately.
	 * Used during restart to avoid waiting for sleep timeouts.
	 */
	public static void interruptTimerThreads() {
		synchronized (timerThreads) {
			for (Thread t : timerThreads) {
				if (t != null && t.isAlive()) {
					t.interrupt();
					logger.info("Interrupted timer thread: {}", t.getName());
				}
			}
		}
	}

	/**
	 * Register a timer thread for interrupt during restart.
	 */
	public static void registerTimerThread(Thread thread) {
		synchronized (timerThreads) {
			timerThreads.add(thread);
		}
	}
}
