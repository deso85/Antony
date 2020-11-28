package bot.antony;

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

public class Antony extends ListenerAdapter {

	public static Antony INSTANCE;
	private static Logger logger = LoggerFactory.getLogger(Antony.class);
	private static CommandManager cmdMan;
	private static String version = "1.2.0";
	private static boolean prodStage = false;

	
	/**
	 * This is the method where the program starts.
	 */
	public static void main(String[] args) {

		try {
			
			JDA jda = JDABuilder.createDefault(getToken(isProdStage())) // The token of the account that is logging in.
					.addEventListeners(new CommandListener()) // An instance of a class that will handle events.
					.build();

			jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.

			jda.getPresence().setStatus(OnlineStatus.ONLINE);
			
			logger.info("Antony (" + getVersion() + ") started");

			cmdMan = new CommandManager();
			
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static String getToken(boolean stage) {
		InputStream is = Antony.class.getResourceAsStream("/antony.properties");
		Properties prop = new Properties();
		try {
			prop.load(is);
			if(stage) {
				return prop.getProperty("discord.token.prod");
			}
			return prop.getProperty("discord.token.dev");
		} catch (IOException e) {
			return null;
		}
	}

	public static String getVersion() {
		return version;
	}

	public static CommandManager getCmdMan() {
		return cmdMan;
	}

	public static boolean isProdStage() {
		return prodStage;
	}
	
	public static Logger getLogger() {
		return logger;
	}
}
