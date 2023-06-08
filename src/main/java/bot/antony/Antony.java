package bot.antony;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.antony.commands.aam.events.OfferListener;
import bot.antony.commands.aam.events.ProposalListener;
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

public class Antony extends ListenerAdapter {

	public static Antony INSTANCE;
	private static Color baseColor = new Color(31, 89, 152);
	private static String cmdPrefix;
	private static long notificationPendingTime;
	private static String version = getProperty("bot.version");
	private static String dataPath;
	private static Logger logger = LoggerFactory.getLogger(Antony.class);
	private static CommandManager cmdMan;
	private static ReactionManager reactionMan;
	private static AntcheckController antcheckController;
	private static NotificationController notificationController;
	private static WhiteListController whitelistController;
	private static WatchListController watchlistController;
	private static BlackListController blacklistController;
	private static SoftbanController softbanController;
	private static GuildController guildController;
	private static UserController userController;
	private static AAMHBController hbController;
	private static GiveawayController gaController;
	private static ReminderController reminderController;
	private static int usercount;
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
		
		//set variables
		cmdPrefix = getProperty("command.prefix");
		notificationPendingTime = Long.parseLong(getProperty("notification.pending.time"));
		dataPath = getProperty("flatfile.path");
		cmdMan = new CommandManager();
		reactionMan = new ReactionManager();
		antcheckController = new AntcheckController();
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
		
		try {
			// build bot
			JDA jda = JDABuilder.createDefault(getProperty("bot.token"))	// The token of the account that is logging in.
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
			//Thread which is used to do timed actions
			Thread timerThread = new Thread() {
				public void run() {
					while(jda.getPresence().getStatus() == OnlineStatus.ONLINE) {
						try {
							notificationController.sendPendingNotifications(jda);
							hbController.checkHBs();
							Thread.sleep(60000);	//60sec
						} catch (InterruptedException e) {
							logger.error("Wasn't able to put Thread asleep.", e);
						}
					}
				}
			};
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
	 * Function to get the ReactionManager
	 * @return	ReactionManager
	 */
	public static ReactionManager getReactionMan() {
		return reactionMan;
	}
	
	public static AntcheckController getAntcheckController() {
		return antcheckController;
	}
	
	/**
	 * Function to get the NotificationManager
	 * @return NotificationManager
	 */
	public static NotificationController getNotificationController() {
		return notificationController;
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
	
	public static AAMHBController getHBController() {
		return hbController;
	}
	
	public static GiveawayController getGiveawayController() {
		return gaController;
	}
	
	public static ReminderController getReminderController() {
		return reminderController;
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
