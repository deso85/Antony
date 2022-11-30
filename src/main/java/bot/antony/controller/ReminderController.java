package bot.antony.controller;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.commands.reminder.Reminder;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Controller to start, monitor and end {@link bot.antony.commands.reminder.Reminder Reminder}
 *
 * @since  7.7.0
 * @author deso85
 */
public class ReminderController {

	private List<Reminder> reminders = new ArrayList<Reminder>();
	private String reminderListFileName = "antony.reminder.json";
	private boolean isRunning = false;
	
	/**
     * Constructs a new ReminderController instance, which can be used to start, monitor and end {@link bot.antony.commands.reminder.Reminder Reminder}.
     */
	public ReminderController() {
		Antony.getLogger().info("Created reminder controller.");
	}
	
	/**
     * Starts a {@link java.lang.Thread Thread} which checks every 5 minutes
     * if {@link bot.antony.commands.reminder.Reminder Reminder} ended and reminds
     * the user
     * 
     * @param  jda
     *         The {@link net.dv8tion.jda.api.JDA JDA} instance is needed to get
     *         necessary objects like {@link net.dv8tion.jda.api.entities.Guild Guilds}
     *         where {@link bot.antony.commands.reminder.Reminder Reminder} will be sent
     */
	public void run(JDA jda) {
		if(!isRunning && reminders.size() > 0) {
			isRunning = true;
			Antony.getLogger().info("[Reminder Controller] Starting Runner");
			Thread timerThread = new Thread() {
				public void run() {
					while(jda.getPresence().getStatus() == OnlineStatus.ONLINE
							&& reminders.size() > 0) {
						try {
							for(Reminder reminder : new ArrayList<Reminder>(reminders)) {
								if(reminder.hasEnded()) {
									Guild guild = jda.getGuildById(reminder.getGuildID());
									
									if(guild.getMemberById(reminder.getUserID()) != null
											&& guild.getTextChannelById(reminder.getChanID()) != null) {
										User user = jda.getUserById(reminder.getUserID());
										TextChannel channel = guild.getTextChannelById(reminder.getChanID());
										
										channel.sendMessage("⏰ Reminder " + user.getAsMention() + ":\n" + reminder.getReminderText()).queue();
									}
									
									removeReminder(reminder);
								}
							}
							Thread.sleep(60000); //1min
						} catch (InterruptedException e) {
							Antony.getLogger().error("Wasn't able to put Thread asleep.", e);
						}
					}
					isRunning = false;
					Antony.getLogger().info("[Reminder Controller] Stopping Runner because there are no more reminder");
				}
			};
			timerThread.start();
		}
	}
	
	/**
	* Adds a {@link bot.antony.commands.reminder.Reminder Reminder}
	*
	* @param  message
	*         Message which includes all relevant information except the runtime
	* @param  runtimeMin
	*         Specifies how long the reminder will last in minutes.
	*/
	public void addReminder(Message message, int runtimeMin) {
		reminders.add(new Reminder(message, runtimeMin));
		save();
		run(message.getJDA());
	}
	
	/**
	* Removes a {@link bot.antony.commands.reminder.Reminder Reminder} from the list of active
	* reminders and stores it.
	*
	* @param  reminder
	*/
	public void removeReminder(Reminder reminder) {
		reminders.remove(reminder);
		save();
	}
	
	/**
	* Loads the list of active {@link bot.antony.commands.reminder.Reminder Reminders}.
	*/
	@SuppressWarnings("unchecked")
	public void load() {
		reminders = (List<Reminder>) Utils.loadJSONData(reminderListFileName, new TypeReference<List<Reminder>>(){}, reminders);
	}
	
	/**
	* Saves the list of active {@link bot.antony.commands.reminder.Reminder Reminders}.
	*/
	public void save() {
		Utils.saveJSONData(reminderListFileName, reminders);
	}
	
}
