package bot.antony;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.antony.events.CommandListener;
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
	private static String version;
	private static Logger logger = LoggerFactory.getLogger(Antony.class);
	private static CommandManager cmdMan = new CommandManager();
	private static boolean prodStage = false;
	

	/**
	 * This is the method where the program starts.
	 * @param args
	 */
	public static void main(String[] args) {
		
		setVersion(getProperty("bot.version"));			// set Antony version
		setCmdPrefix(getProperty("command.prefix"));	// set command prefix
		
		try {
			// build bot
			JDA jda = JDABuilder.createDefault(getToken(isProdStage()))	// The token of the account that is logging in.
					.addEventListeners(new CommandListener())			// An instance of a class that will handle events.
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
			
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}
