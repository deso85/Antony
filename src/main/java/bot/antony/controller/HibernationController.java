package bot.antony.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.commands.hibernation.Hibernation;
import bot.antony.commands.hibernation.Hibernation.Notification;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Controller to store {@link Hibernation Hibernations} and to remind users
 * to start or end the hibernation of their ants.
 * Reminders are sent as private message. If that fails they are sent to the channel
 * in which the hibernation was created.
 *
 * @since  7.23.0
 */
public class HibernationController {

	/** Maximum number of hibernations per user and guild. */
	public static final int MAX_ENTRIES_PER_USER = 10;
	private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
	private static final long CHECK_INTERVAL_MS = 10 * 60 * 1000;	// 10 min

	private final String fileName = "antony.hibernation.json";
	private List<Hibernation> hibernations = new ArrayList<Hibernation>();

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public HibernationController() {
		super();
	}

	// --------------------------------------------------
	// Thread
	// --------------------------------------------------
	/**
	 * Starts a thread which checks every 10 minutes if reminders are due.
	 */
	public void run(JDA jda) {
		Thread timerThread = new Thread() {
			public void run() {
				while(jda.getPresence().getStatus() == OnlineStatus.ONLINE) {
					try {
						checkReminders(jda, LocalDateTime.now(ZONE));
						Thread.sleep(CHECK_INTERVAL_MS);
					} catch (InterruptedException e) {
						Antony.getLogger().info("Hibernation timer thread interrupted (likely during restart).");
						Thread.currentThread().interrupt();
						break;
					} catch (Exception e) {
						// Never let the thread die because of an unexpected error
						Antony.getLogger().error("Unexpected error while checking hibernation reminders.", e);
					}
				}
			}
		};
		timerThread.setName("antony-hibernation-timer");
		timerThread.setDaemon(true);
		Antony.registerTimerThread(timerThread);
		timerThread.start();
	}

	/**
	 * Sends all reminders which are due.
	 *
	 * @param  now
	 *         current local date and time (Europe/Berlin)
	 */
	public synchronized void checkReminders(JDA jda, LocalDateTime now) {
		boolean changed = false;
		for(Hibernation hibernation : getAll()) {
			Guild guild = jda.getGuildById(hibernation.getGuildID());
			if(guild == null) {
				continue;	// guild not available at the moment
			}

			Notification due = hibernation.takeDueNotification(now);
			if(due == null) {
				continue;
			}
			changed = true;

			Member member = guild.getMemberById(hibernation.getUserID());
			if(member == null) {
				Antony.getLogger().info("[Hibernation] User " + hibernation.getUserID() + " is no member of guild "
						+ guild.getName() + " anymore. Hibernation " + hibernation.getId() + " removed.");
				remove(hibernation, false);
				continue;
			}

			send(guild, member.getUser(), hibernation, hibernation.getNotificationText(due, guild.getName()));

			if(hibernation.isCycleFinished()) {
				if(hibernation.isYearly()) {
					hibernation.nextCycle();
				} else {
					remove(hibernation, false);
				}
			}
		}
		if(changed) {
			save();
		}
	}

	/**
	 * Sends the text as private message. If that fails it is sent to the channel
	 * where the hibernation was created and the user is mentioned.
	 */
	private void send(Guild guild, User user, Hibernation hibernation, String text) {
		user.openPrivateChannel()
			.flatMap(pc -> pc.sendMessage(text))
			.queue(
				msg -> Antony.getLogger().info("[Hibernation] Sent reminder to " + user.getName() + " (" + user.getId() + ")."),
				error -> {
					TextChannel channel = guild.getTextChannelById(hibernation.getChanID());
					if(channel == null || !channel.canTalk()) {
						Antony.getLogger().warn("[Hibernation] Could not send reminder to " + user.getName() + " (" + user.getId() + "): "
								+ error.getMessage());
						return;
					}
					channel.sendMessage(user.getAsMention() + " " + text)
						.setAllowedMentions(Collections.emptyList())
						.mentionUsers(user.getIdLong())
						.queue(null, e -> Antony.getLogger().warn("[Hibernation] Could not send reminder to channel "
								+ channel.getName() + ": " + e.getMessage()));
				});
	}

	// --------------------------------------------------
	// Data handling
	// --------------------------------------------------
	public synchronized void add(Hibernation hibernation) {
		hibernations.add(hibernation);
		save();
	}

	public synchronized void remove(Hibernation hibernation) {
		remove(hibernation, true);
	}

	private synchronized void remove(Hibernation hibernation, boolean persist) {
		hibernations.remove(hibernation);
		if(persist) {
			save();
		}
	}

	/**
	 * @return a copy of all stored hibernations
	 */
	public synchronized List<Hibernation> getAll() {
		return new ArrayList<Hibernation>(hibernations);
	}

	/**
	 * @return the hibernations of the user in the guild, ordered by creation
	 */
	public synchronized List<Hibernation> getHibernations(String guildID, String userID) {
		return hibernations.stream()
				.filter(h -> guildID.equals(h.getGuildID()) && userID.equals(h.getUserID()))
				.sorted((a, b) -> Long.compare(a.getId(), b.getId()))
				.collect(Collectors.toList());
	}

	/**
	 * @return a new unique id
	 */
	public synchronized long nextId() {
		long id = System.currentTimeMillis();
		for(Hibernation h : hibernations) {
			if(h.getId() >= id) {
				id = h.getId() + 1;
			}
		}
		return id;
	}

	@SuppressWarnings("unchecked")
	public synchronized void load() {
		hibernations = (List<Hibernation>) Utils.loadJSONData(fileName, new TypeReference<List<Hibernation>>(){}, hibernations);
		Antony.getLogger().info("[Hibernation] Loaded " + hibernations.size() + " hibernations.");
	}

	public synchronized void save() {
		Utils.saveJSONData(fileName, hibernations);
	}
}
