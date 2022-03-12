package bot.antony;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import com.jagrosh.jdautilities.examples.command.GuildlistCommand;
import com.jagrosh.jdautilities.examples.command.RoleinfoCommand;
import com.jagrosh.jdautilities.examples.command.ServerinfoCommand;

import bot.antony.commands.PingCommand;
import bot.antony.commands.ShutdownCommand;
import bot.antony.db.DbController;
import bot.antony.db.entities.GuildEntity;
import bot.antony.db.entities.UserEntity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Antony extends ListenerAdapter {

	private static JDA jda = null;
	private static DbController dbController;
	private static Logger logger = LoggerFactory.getLogger(Antony.class);
	private static boolean prodStage = false;

	/**
	 * This is the method where the program starts.
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		try {

			long enable = System.currentTimeMillis();

			new Antony();

			String stage = prodStage ? "PROD" : "DEV/TEST";
			logger.info("[" + stage + "] " + getBotName() + " (v" + getVersion() + ") started in "
					+ (System.currentTimeMillis() - enable) + "ms!");

			Session session = dbController.getSessionFactory().openSession();
			Transaction tx = session.beginTransaction();
			
			try {

				GuildEntity myGuild = new GuildEntity(1, "Gilde 1");
				GuildEntity yourGuild = new GuildEntity(2, "Gilde 2");

				UserEntity usr1 = new UserEntity(1, "Hans");
				UserEntity usr2 = new UserEntity(2, "Martin");

				session.saveOrUpdate(myGuild);
				session.saveOrUpdate(yourGuild);
				session.saveOrUpdate(usr1);
				session.saveOrUpdate(usr2);

				session.flush();
				tx.commit();

				//List<GuildEntity> guildList = session.createQuery("from GuildEntity").list();
				Query<GuildEntity> query = session.createNamedQuery("GuildEntity.SelectAll");
				//List<GuildEntity> guildList = session.createNamedQuery("SelectAll", GuildEntity.class).getResultList();
				List<GuildEntity> guildList = query.getResultList();
				for (GuildEntity guild : guildList) {
					System.out.println("Id: " + guild.getId() + " | Name:" + guild.getName());
				}
				List<UserEntity> userList = session.createQuery("from UserEntity").list();
				for (UserEntity user : userList) {
					System.out.println("Id: " + user.getId() + " | Name:" + user.getName());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				tx.rollback();
			} finally {
				if (session != null) {
					session.close();
				}
			}


		} catch (LoginException e) {
			logger.error("Could not login to Discord!", e);
		} catch (InterruptedException e) {
			logger.error("Antony thread is interrupted while waiting!", e);
		}

	}

	private Antony() throws LoginException, InterruptedException {
		// build bot
		JDA jda = getJDA();
		String helpWord = getBotName().toLowerCase();

		CommandClientBuilder builder = new CommandClientBuilder();
		builder.setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26");
		builder.setOwnerId(getProperty("bot.owner.id"));
		builder.setActivity(Activity.listening(getCommandPrefix() + helpWord));
		builder.setPrefix(getCommandPrefix());
		builder.setHelpWord(helpWord);
		builder.setStatus(OnlineStatus.ONLINE);

		builder.addCommand(new AboutCommand(Color.CYAN, "ein schöner Bot", new String[] { "a", "b" }, null));
		builder.addCommand(new GuildlistCommand(new EventWaiter()));
		builder.addCommand(new RoleinfoCommand());
		builder.addCommand(new ServerinfoCommand());
		builder.addCommand(new PingCommand());
		builder.addCommand(new ShutdownCommand());

		jda.addEventListener(builder.build());
		jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.
		
		dbController = new DbController(getProperty("sqlite.db.path"));
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------

	private static JDA getJDA() throws LoginException {
		if (jda == null) {
			jda = JDABuilder.createDefault(getToken(prodStage)) // The token of the account that is logging in.
					.setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
					.setMemberCachePolicy(MemberCachePolicy.ALL) // ignored if chunking enabled
					.enableCache(CacheFlag.ACTIVITY) // To get details on guild members
					.enableCache(CacheFlag.CLIENT_STATUS) // To get client status
					.enableCache(CacheFlag.EMOTE) // To get guilds emotes
					.enableIntents(GatewayIntent.GUILD_MEMBERS) // Has to be set to use MemberCachePolicy.ALL
					.enableIntents(GatewayIntent.GUILD_PRESENCES) // Has to be set to use CacheFlag.ACTIVITY
					.build();
		}
		return jda;
	}

	/**
	 * Function to get value for key from antony.properties file
	 * 
	 * @param key as String
	 * @return value as String
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
	 * 
	 * @param prod Boolean if bot is for productive use
	 * @return Discord Bot Token as String
	 */
	private static String getToken(boolean prod) {
		if (prod) {
			return getProperty("bot.token.prod");
		}
		return getProperty("bot.token.dev");
	}

	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	private static String getBotName() {
		return jda.getSelfUser().getName();
	}

	private static String getCommandPrefix() {
		return getProperty("command.prefix");
	}

	private static String getVersion() {
		return getProperty("bot.version");
	}

}
