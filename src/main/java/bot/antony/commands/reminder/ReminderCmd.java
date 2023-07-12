package bot.antony.commands.reminder;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

/**
 * Command to interact with the {@link bot.antony.controller.ReminderController ReminderController}.
 *
 * @since  7.7.0
 * @author deso85
 */
public class ReminderCmd extends ServerCommand {
	
	/**
     * Constructs a new ReminderCmd instance, which can be used to interact with the {@link bot.antony.controller.ReminderController ReminderController}.
     */
	public ReminderCmd() {
		super();
		this.privileged = false;
		this.name = "reminder";
		this.description = "Mit diesem Befehl lassen sich Reminder hinterlegen, die einem nach Ablauf der Zeit an etwas erinnern sollen.";
		this.shortDescription = "Befehl zur Anlage eines Reminders.";
		this.example = "1d 2h 3m Aktualisiere deinen Lasius niger HB";
	}
	
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		
		int pim = parseDuration(message.getContentDisplay());
		if(userMessage.length >= 2 && pim > 0) {
			Antony.getReminderController().addReminder(message, pim);
			message.reply("Ich erinnere dich ca. am <t:" + (Instant.now().getEpochSecond() + pim*60) + ":f> Uhr").queue();
		} else {
			printHelp(channel);
		}
	}
	
	
	/**
	 * Function to parse the user input for {@link bot.antony.commands.reminder.Reminder Reminder} duration.
	 * 
	 * @param  usrInput
	 *         String to parse.
	 * @return int
	 *         duration in minutes.
	 */
	private int parseDuration(String usrInput) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("[ReminderCmd] Analyze user input for duration: " + usrInput + " -> ");
		
		int pim = 0;
		Pattern pattern = Pattern.compile("\\d+[dhm]");
		Matcher matcher = pattern.matcher(usrInput);
		while(matcher.find()) {
			String unit = matcher.group();
			char timeType = unit.charAt(unit.length() - 1);
			int timeAmount = Integer.parseInt(unit.substring(0, unit.length() - 1));
			if(timeType == 'd') {
				pim += TimeUnit.DAYS.toMinutes(timeAmount);
				logMsg.append(TimeUnit.DAYS.toDays(timeAmount) + " DAYS ");
			}
	        
			if(timeType == 'h') {
				pim += TimeUnit.HOURS.toMinutes(timeAmount);
				logMsg.append(TimeUnit.HOURS.toHours(timeAmount) + " HOURS ");
			}
	        
			if(timeType == 'm') {
				pim += TimeUnit.MINUTES.toMinutes(timeAmount);
				logMsg.append(TimeUnit.MINUTES.toMinutes(timeAmount) + " MINUTES ");
			}
		}
		logMsg.append(" --> PIM: " + pim);
		Antony.getLogger().debug(logMsg.toString());
		return pim;
	}
	
}