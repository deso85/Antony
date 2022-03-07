package bot.antony;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.antony.events.CommandListener;
import bot.antony.events.GuildMemberJoin;
import bot.antony.events.GuildMemberLeave;
import bot.antony.events.GuildMemberUpdateNickname;
import bot.antony.events.GuildUpdateName;
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

	
	private static boolean prodStage = false;
	private static final Logger logger = LoggerFactory.getLogger(Antony.class);

	/**
	 * This is the method where the program starts.
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			// build bot
			JDA jda = JDABuilder.createDefault(getToken(prodStage))	// The token of the account that is logging in.
					.addEventListeners(new CommandListener())			// Listener for commands
					.addEventListeners(new MessageReceived())
					.addEventListeners(new MessageUpdate())
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
			int usercount = 0;
			for(Guild guild: jda.getGuilds()) {
				usercount += guild.getMemberCount();
			}
			jda.getPresence().setActivity(Activity.listening("!" + "antony | " + usercount + " User | " + jda.getGuilds().size() + " Server"));

			
			// Create log output after startup
			String stage = "PROD";
			if(!prodStage) {
				stage = "DEV/TEST";
			}
			String postStartLogEntry = "[" + stage + "] Antony (v" + "6.0.0" + ") started";
			logger.info(postStartLogEntry);
			
			
		} catch (LoginException e) {
			logger.error("Could not login to Discord!", e);
		} catch (InterruptedException e) {
			logger.error("Antony thread is interrupted while waiting!", e);
		}
		
	}
	
	private Antony() {
		
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
	
	
}
