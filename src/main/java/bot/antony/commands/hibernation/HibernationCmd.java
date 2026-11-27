package bot.antony.commands.hibernation;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.controller.HibernationController;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

/**
 * Command to interact with the {@link bot.antony.controller.HibernationController HibernationController}.
 * Users can store the hibernation (Winterruhe) of their ants and get reminded on start and end.
 *
 * @since  7.23.0
 */
public class HibernationCmd extends ServerCommand {

	/** Maximum length of the description (e.g. species name). */
	public static final int MAX_DESCRIPTION_LENGTH = 100;
	private static final List<String> FLAGS_YEARLY = Arrays.asList("jährlich", "jaehrlich", "yearly");
	private static final List<String> FLAGS_PRE_REMINDER = Arrays.asList("vorwarnung", "vorher", "prereminder");

	/**
	 * Constructs a new HibernationCmd instance.
	 */
	public HibernationCmd() {
		super();
		this.privileged = false;
		this.name = "winterruhe";
		this.description = "Mit diesem Befehl kannst du dich an den Beginn und das Ende der Winterruhe deiner Ameisen erinnern lassen. "
				+ "Die Erinnerung kommt am jeweiligen Tag ab " + Hibernation.SEND_TIME + " Uhr per Privatnachricht. "
				+ "Sind Privatnachrichten deaktiviert, erinnere ich dich im Kanal, in dem du den Befehl geschrieben hast.";
		this.shortDescription = "Erinnerung an Beginn und Ende der Winterruhe.";
		this.example = "01.11. 01.03. jährlich vorwarnung Lasius niger";
		this.cmdParams.put("(TT.MM.) (TT.MM.) [jährlich] [vorwarnung] (Beschreibung)",
				"Legt eine Winterruhe vom ersten bis zum zweiten Datum an. "
				+ "*jährlich*: Die Erinnerung wiederholt sich jedes Jahr. "
				+ "*vorwarnung*: Zusätzliche Erinnerung " + Hibernation.PRE_REMINDER_DAYS + " Tage vor Beginn und Ende, um die Temperatur langsam anzupassen.");
		this.cmdParams.put("liste", "Zeigt deine hinterlegten Winterruhen.");
		this.cmdParams.put("löschen (Nr)", "Löscht die Winterruhe mit der Nummer aus der Liste.");
	}

	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		String[] args = message.getContentDisplay().trim().split("\\s+");
		if(args.length < 2) {
			printHelp(channel);
			return;
		}

		switch(args[1].toLowerCase()) {
			case "liste":
			case "list":
				printList(member, message);
				break;
			case "löschen":
			case "loeschen":
			case "delete":
				delete(member, message, args);
				break;
			default:
				create(member, channel, message, args);
				break;
		}
	}

	private void create(Member member, GuildMessageChannel channel, Message message, String[] args) {
		MonthDay start = args.length > 1 ? Hibernation.parseMonthDay(args[1]) : null;
		MonthDay end = args.length > 2 ? Hibernation.parseMonthDay(args[2]) : null;
		if(start == null || end == null) {
			printHelp(channel);
			return;
		}
		if(start.equals(end)) {
			reply(message, "Beginn und Ende der Winterruhe dürfen nicht am selben Tag sein.");
			return;
		}

		boolean yearly = false;
		boolean preReminder = false;
		int i = 3;
		for(; i < args.length; i++) {
			String arg = args[i].toLowerCase();
			if(FLAGS_YEARLY.contains(arg)) {
				yearly = true;
			} else if(FLAGS_PRE_REMINDER.contains(arg)) {
				preReminder = true;
			} else {
				break;
			}
		}
		String description = String.join(" ", Arrays.copyOfRange(args, i, args.length)).trim();
		if(description.isEmpty()) {
			reply(message, "Bitte gib eine Beschreibung an, z.B. die Art deiner Kolonie.\nBeispiel: `"
					+ Antony.getCmdPrefix() + name + " " + example + "`");
			return;
		}
		if(description.length() > MAX_DESCRIPTION_LENGTH) {
			reply(message, "Die Beschreibung darf höchstens " + MAX_DESCRIPTION_LENGTH + " Zeichen lang sein.");
			return;
		}

		HibernationController controller = Antony.getHibernationController();
		String guildID = member.getGuild().getId();
		String userID = member.getId();
		if(controller.getHibernations(guildID, userID).size() >= HibernationController.MAX_ENTRIES_PER_USER) {
			reply(message, "Du hast bereits " + HibernationController.MAX_ENTRIES_PER_USER + " Winterruhen hinterlegt. "
					+ "Lösche zuerst eine mit `" + Antony.getCmdPrefix() + name + " löschen (Nr)`.");
			return;
		}

		LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));
		Hibernation hibernation = new Hibernation(controller.nextId(), guildID, userID, channel.getIdLong(),
				description, start, end, yearly, preReminder, today);
		controller.add(hibernation);
		Antony.getLogger().info("[Hibernation] " + member.getUser().getName() + " (" + userID + ") created hibernation "
				+ hibernation.getId() + " in guild " + member.getGuild().getName() + ".");

		StringBuilder sb = new StringBuilder();
		sb.append("Winterruhe für **" + description + "** gespeichert: ");
		sb.append(Hibernation.format(hibernation.getStartDate()) + " bis " + Hibernation.format(hibernation.getEndDate()));
		sb.append(yearly ? " (jährlich)" : "");
		sb.append(".\n");
		if(hibernation.isStartSent()) {
			sb.append("Die Winterruhe läuft bereits, ich erinnere dich an das Ende.");
		} else {
			sb.append("Ich erinnere dich an Beginn und Ende");
			sb.append(preReminder ? ", jeweils auch " + Hibernation.PRE_REMINDER_DAYS + " Tage vorher." : ".");
		}
		sb.append("\n*Tipp: Erlaube Privatnachrichten von Servermitgliedern, damit dich die Erinnerung direkt erreicht.*");
		reply(message, sb.toString());
	}

	private void printList(Member member, Message message) {
		List<Hibernation> list = Antony.getHibernationController().getHibernations(member.getGuild().getId(), member.getId());
		if(list.isEmpty()) {
			reply(message, "Du hast keine Winterruhe hinterlegt. Mehr dazu mit `" + Antony.getCmdPrefix() + name + "`.");
			return;
		}
		StringBuilder sb = new StringBuilder("**Deine Winterruhen:**\n");
		for(int i = 0; i < list.size(); i++) {
			Hibernation h = list.get(i);
			sb.append("`" + (i + 1) + "` **" + h.getDescription() + "**: ");
			sb.append(Hibernation.format(h.getStartDate()) + " bis " + Hibernation.format(h.getEndDate()));
			if(h.isYearly()) {
				sb.append(", jährlich");
			}
			if(h.isPreReminder()) {
				sb.append(", mit Vorwarnung");
			}
			sb.append("\n");
		}
		reply(message, sb.toString());
	}

	private void delete(Member member, Message message, String[] args) {
		List<Hibernation> list = Antony.getHibernationController().getHibernations(member.getGuild().getId(), member.getId());
		int nr;
		try {
			nr = Integer.parseInt(args.length > 2 ? args[2] : "");
		} catch (NumberFormatException e) {
			nr = -1;
		}
		if(nr < 1 || nr > list.size()) {
			reply(message, "Bitte gib eine gültige Nummer aus `" + Antony.getCmdPrefix() + name + " liste` an.");
			return;
		}
		Hibernation h = list.get(nr - 1);
		Antony.getHibernationController().remove(h);
		reply(message, "Winterruhe für **" + h.getDescription() + "** gelöscht.");
	}

	/** Replies without pinging anyone (the description is user input). */
	private void reply(Message message, String text) {
		message.reply(text).setAllowedMentions(Collections.emptyList()).mentionRepliedUser(false).queue();
	}
}
