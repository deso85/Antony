package bot.antony.commands.hibernation;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A hibernation (Winterruhe) of a users ant colony which is stored by the
 * {@link bot.antony.controller.HibernationController HibernationController}.
 * <p>
 * An entry always describes the current (or next) hibernation cycle with a concrete start and end date.
 * Depending on the settings the user gets reminded 7 days before the start/end and on the start/end date.
 * Yearly entries move on to the next cycle after the end date has been reached.
 *
 * @since  7.23.0
 */
public class Hibernation {

	/** Days before start/end date on which the optional pre-reminder is sent. */
	public static final int PRE_REMINDER_DAYS = 7;
	/** Earliest time of day on which reminders are sent (to not wake people up at midnight). */
	public static final LocalTime SEND_TIME = LocalTime.of(9, 0);

	/** Type of a reminder in the order in which they are due. */
	public enum Notification { PRE_START, START, PRE_END, END }

	private long id;
	private String guildID;
	private String userID;
	private long chanID;
	private String description;
	private int startMonth;
	private int startDay;
	private int endMonth;
	private int endDay;
	private boolean yearly;
	private boolean preReminder;
	private LocalDate startDate;
	private LocalDate endDate;
	private boolean preStartSent;
	private boolean startSent;
	private boolean preEndSent;
	private boolean endSent;

	/**
	 * Constructs an empty instance (needed to load the data from JSON).
	 */
	public Hibernation() {
		super();
	}

	/**
	 * Constructs a new hibernation and calculates the current or next cycle based on today.
	 * Reminders whose date has already passed are marked as sent.
	 *
	 * @throws IllegalArgumentException if start and end are the same day
	 */
	public Hibernation(long id, String guildID, String userID, long chanID, String description,
			MonthDay start, MonthDay end, boolean yearly, boolean preReminder, LocalDate today) {
		if(start.equals(end)) {
			throw new IllegalArgumentException("Start and end of the hibernation must not be the same day.");
		}
		this.id = id;
		this.guildID = guildID;
		this.userID = userID;
		this.chanID = chanID;
		this.description = description;
		this.startMonth = start.getMonthValue();
		this.startDay = start.getDayOfMonth();
		this.endMonth = end.getMonthValue();
		this.endDay = end.getDayOfMonth();
		this.yearly = yearly;
		this.preReminder = preReminder;

		// Use the first cycle which has not ended yet (it might already be running)
		for(int year = today.getYear() - 1; year <= today.getYear() + 1; year++) {
			setCycle(year);
			if(!endDate.isBefore(today)) {
				break;
			}
		}
		this.preStartSent = !preReminder || getDate(Notification.PRE_START).isBefore(today);
		this.startSent = startDate.isBefore(today);
		this.preEndSent = !preReminder || getDate(Notification.PRE_END).isBefore(today);
		this.endSent = false;
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	/**
	 * Parses a date like "1.11", "01.11." or "29.02".
	 *
	 * @return the parsed day and month or null if the input is invalid
	 */
	public static MonthDay parseMonthDay(String input) {
		if(input == null || !input.matches("\\d{1,2}\\.\\d{1,2}\\.?")) {
			return null;
		}
		String[] parts = input.split("\\.");
		try {
			return MonthDay.of(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
		} catch (DateTimeException e) {
			return null;
		}
	}

	private void setCycle(int startYear) {
		MonthDay start = MonthDay.of(startMonth, startDay);
		MonthDay end = MonthDay.of(endMonth, endDay);
		startDate = start.atYear(startYear);	// 29.02. becomes 28.02. in non leap years
		endDate = end.atYear(startYear);
		if(!endDate.isAfter(startDate)) {
			endDate = end.atYear(startYear + 1);
		}
	}

	/**
	 * @return the date on which the given reminder is due
	 */
	@JsonIgnore
	public LocalDate getDate(Notification notification) {
		switch(notification) {
			case PRE_START: return startDate.minusDays(PRE_REMINDER_DAYS);
			case START: return startDate;
			case PRE_END: return endDate.minusDays(PRE_REMINDER_DAYS);
			default: return endDate;
		}
	}

	private boolean isSent(Notification notification) {
		switch(notification) {
			case PRE_START: return preStartSent;
			case START: return startSent;
			case PRE_END: return preEndSent;
			default: return endSent;
		}
	}

	private void markSent(Notification notification) {
		switch(notification) {
			case PRE_START: preStartSent = true; break;
			case START: startSent = true; break;
			case PRE_END: preEndSent = true; break;
			default: endSent = true; break;
		}
	}

	private boolean isDue(Notification notification, LocalDateTime now) {
		LocalDate date = getDate(notification);
		return date.isBefore(now.toLocalDate())
				|| (date.isEqual(now.toLocalDate()) && !now.toLocalTime().isBefore(SEND_TIME));
	}

	/**
	 * Returns the reminder which has to be sent now and marks it as sent.
	 * If several reminders are due (e.g. because the bot was offline) only the latest one is returned,
	 * the older ones are outdated and only marked as sent.
	 *
	 * @param  now
	 *         current local date and time
	 * @return the reminder to send or null if nothing is due
	 */
	public Notification takeDueNotification(LocalDateTime now) {
		Notification due = null;
		for(Notification notification : Notification.values()) {
			if(!isSent(notification) && isDue(notification, now)) {
				markSent(notification);
				due = notification;
			}
		}
		return due;
	}

	/**
	 * @return true if all reminders of the current cycle have been sent
	 */
	@JsonIgnore
	public boolean isCycleFinished() {
		return endSent;
	}

	/**
	 * Moves a yearly hibernation to the next cycle and resets the reminders.
	 */
	public void nextCycle() {
		setCycle(startDate.getYear() + 1);
		preStartSent = !preReminder;
		startSent = false;
		preEndSent = !preReminder;
		endSent = false;
	}

	/**
	 * @return the text of the reminder which is sent to the user
	 */
	public String getNotificationText(Notification notification, String guildName) {
		StringBuilder sb = new StringBuilder();
		switch(notification) {
			case PRE_START:
				sb.append("⏰ In " + PRE_REMINDER_DAYS + " Tagen (am " + format(startDate) + ") beginnt die Winterruhe für **" + description + "**.\n");
				sb.append("Denk daran, die Temperatur langsam abzusenken.");
				break;
			case START:
				sb.append("❄️ Heute beginnt die Winterruhe für **" + description + "**.\n");
				sb.append("Sie endet am " + format(endDate) + ".");
				break;
			case PRE_END:
				sb.append("⏰ In " + PRE_REMINDER_DAYS + " Tagen (am " + format(endDate) + ") endet die Winterruhe für **" + description + "**.\n");
				sb.append("Du kannst die Temperatur langsam wieder erhöhen.");
				break;
			default:
				sb.append("🌱 Heute endet die Winterruhe für **" + description + "**.");
				if(yearly) {
					sb.append("\nIch erinnere dich nächstes Jahr wieder.");
				}
				break;
		}
		sb.append("\n*(Server: " + guildName + ")*");
		return sb.toString();
	}

	public static String format(LocalDate date) {
		return String.format("%02d.%02d.%d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
	}

	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getGuildID() {
		return guildID;
	}

	public void setGuildID(String guildID) {
		this.guildID = guildID;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public long getChanID() {
		return chanID;
	}

	public void setChanID(long chanID) {
		this.chanID = chanID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getStartMonth() {
		return startMonth;
	}

	public void setStartMonth(int startMonth) {
		this.startMonth = startMonth;
	}

	public int getStartDay() {
		return startDay;
	}

	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}

	public int getEndMonth() {
		return endMonth;
	}

	public void setEndMonth(int endMonth) {
		this.endMonth = endMonth;
	}

	public int getEndDay() {
		return endDay;
	}

	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}

	public boolean isYearly() {
		return yearly;
	}

	public void setYearly(boolean yearly) {
		this.yearly = yearly;
	}

	public boolean isPreReminder() {
		return preReminder;
	}

	public void setPreReminder(boolean preReminder) {
		this.preReminder = preReminder;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public boolean isPreStartSent() {
		return preStartSent;
	}

	public void setPreStartSent(boolean preStartSent) {
		this.preStartSent = preStartSent;
	}

	public boolean isStartSent() {
		return startSent;
	}

	public void setStartSent(boolean startSent) {
		this.startSent = startSent;
	}

	public boolean isPreEndSent() {
		return preEndSent;
	}

	public void setPreEndSent(boolean preEndSent) {
		this.preEndSent = preEndSent;
	}

	public boolean isEndSent() {
		return endSent;
	}

	public void setEndSent(boolean endSent) {
		this.endSent = endSent;
	}
}
