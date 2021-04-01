package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class AntonyHelp implements ServerCommand {

	private TextChannel channel;

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		setChannel(channel);

		String[] userMessage = message.getContentDisplay().split(" ");

		if (userMessage.length > 1) {

			switch (userMessage[1].toLowerCase()) {

			case "changelog":
				
				// Generate changelog entries
				String cmdPrefix = Antony.getCmdPrefix();
				List<ChangeLogEntry> changeLog = new ArrayList<ChangeLogEntry>();
				changeLog.add(new ChangeLogEntry("01.04.2021 - Version 2.4.1b", "Die Ausgabe des ***" + cmdPrefix + "userinfo*** Befehls wurde für Benutzer ohne Rollen verändert. Wenn eine PN nicht zugestellt werden kann, erhält das Mod-Team nun eine entsprechende Benachrichtigung, um den User darüber informieren zu können. Die Suche nach Ameisen ist nun tolleranter gegenüber Leerzeichen im Namen."));
				changeLog.add(new ChangeLogEntry("28.03.2021 - Version 2.4.0b", "Die moderative Funktion ***" + cmdPrefix + "softban*** wurde hinzugefügt. Hierüber können User verwaltet werden, deren Posts direkt wieder gelöscht werden. Dies ist notwendig, um zu vermeiden, dass z.B. Webhooks missbraucht werden."));
				changeLog.add(new ChangeLogEntry("27.03.2021 - Version 2.3.0b", "Die Funktion ***" + cmdPrefix + "map*** wurde hinzugefügt und zeigt eine Karte von antmaps.org, auf der zu sehen ist, wo auf der Welt die gesuchte Ameisenart vorkommt. Zudem wurde der Scheduling-Mechanismus für die Kanalbenachrichtigungen angepasst."));
				changeLog.add(new ChangeLogEntry("26.03.2021 - Version 2.2.0b", "Moderative Features hinzugefügt: Das Mod-Team wird nun benachrichtigt, wenn eine Nachricht durch einen User für das Mod-Team markiert wird. Informationen zu einem User lassen sich nun einfacher abrufen. Das Angebote-Log wird nun automatisch ausgefüllt, wenn ein User ein Angebot einstellt."));
				changeLog.add(new ChangeLogEntry("25.03.2021 - Version 2.1.0b", "Die moderative Funktion ***" + cmdPrefix + "watchlist*** wurde hinzugefügt, mit der das Mod-Team definierte Begriffe überwachen lassen kann. Wird eines dieser Begriffe im Chat erwähnt, wird das Team automatisch darüber informiert und kann die Inhalte schneller kontrollieren."));
				changeLog.add(new ChangeLogEntry("06.03.2021 - Version 2.0.0b", "Die Funktion ***" + cmdPrefix + "userinfo*** kann jetzt mit dem Parameter \"light\" genutzt werden, wodurch nur wenige Basis-Informationen ausgegeben werden. Zusätzlich wurden erste Vorbereitungen zur Nutzung einer DB getroffen. Darüber hinaus hat der Bot nun einen eigenen Status mit einer kleinen Statistik."));
				changeLog.add(new ChangeLogEntry("26.02.2021 - Version 1.8.0", "Die Funktionen ***" + cmdPrefix + "emergency*** und ***" + cmdPrefix + "shopping*** wurden hinzugefügt. ***" + cmdPrefix + "emergency*** gibt Hinweise zur Ameisenhaltung bei bestimmten Notfällen. ***" + cmdPrefix + "shopping*** gibt Kaufempfehlungen aus, die für die Ameisenhaltung relevant sind."));
				changeLog.add(new ChangeLogEntry("16.02.2021 - Version 1.7.0", "Die Funktion ***" + cmdPrefix + "channel*** wurde hinzugefügt, die aktuell alle Kanäle ausgibt, in denen innerhalb eines bestimmten Zeitraums keine Einträge gemacht wurden."));
				changeLog.add(new ChangeLogEntry("13.02.2021 - Version 1.6.4", "Die URL für Antcheck wurde angepasst, da die Domain umgezogen wurde. Zusätzlich wurden die ***" + cmdPrefix + "notify*** Funktion überarbeitet, um konkurrierende Zugriffe auf gespeicherte Daten zu vermeiden."));
				changeLog.add(new ChangeLogEntry("22.01.2021 - Version 1.6.3", "Die Funktion ***" + cmdPrefix + "pnlink*** wurde implementiert, um Kanäle als Verlinkung in PNs versenden zu können."));
				changeLog.add(new ChangeLogEntry("15.01.2021 - Version 1.6.2", "Die Funktion ***" + cmdPrefix + "notify*** wurde dahingehend angepasst, dass Kanalupdates nun gesammelt und dann gebündelt versendet werden."));
				changeLog.add(new ChangeLogEntry("13.01.2021 - Version 1.6.1", "Bugfix für die Funktion ***" + cmdPrefix + "notify***: Die PN über Einstellungsänderungen wurde nicht versendet, wenn zu viele Kanäle ausgewählt wurden."));
				changeLog.add(new ChangeLogEntry("13.01.2021 - Version 1.6.0", "Die Funktion ***" + cmdPrefix + "giveaway*** wurde in einer ersten Version fertig implementiert."));
				changeLog.add(new ChangeLogEntry("10.01.2021 - Version 1.5.1", "Code und die JSON Strukturen für gespeicherte Daten wurden überarbeitet."));
				changeLog.add(new ChangeLogEntry("06.01.2021 - Version 1.5.0", "Die Funktion ***" + cmdPrefix + "notify*** wurde fertig implementiert. Hierüber können User über neue Einträge in Kanälen benachrichtigt werden."));
				changeLog.add(new ChangeLogEntry("30.12.2020 - Version 1.4.2", "Bugfixes für die ***" + cmdPrefix + "sells*** Funktion."));
				changeLog.add(new ChangeLogEntry("21.12.2020 - Version 1.4.1", "Die Funktionen ***" + cmdPrefix + "userinfo*** und ***" + cmdPrefix + "showavatar*** wurden dahingehend korrigiert, dass nun auch Benutzernamen mit Leerzeichen abgefragt werden können."));
				changeLog.add(new ChangeLogEntry("12.12.2020 - Version 1.4.0", "Die ***" + cmdPrefix + "showavatar*** Funktion wurde implementiert und der Code an einigen Stellen überarbeitet."));
				changeLog.add(new ChangeLogEntry("05.12.2020 - Version 1.3.0", "Die ***" + cmdPrefix + "userinfo*** Funktion wurde fertig implementiert."));
				changeLog.add(new ChangeLogEntry("29.11.2020 - Version 1.2.1", "Kleineres Update aufgrund einer Änderung an der antcheck API"));
				changeLog.add(new ChangeLogEntry("26.11.2020 - Version 1.2.0", "Einige Basis-Funktionalitäten wurden hinzugefügt, um den Bot einfacher nutzen zu können. "
						+ "Dazu zählt unter anderem das ***" + cmdPrefix + "antony*** Kommando, um dem Anwender Informationen zum Bot zur Verfügung zu stellen. "
						+ "Zusätzlich wurden kleinere Bugs behoben."));
				changeLog.add(new ChangeLogEntry("25.11.2020 - Version 1.1.0", "Ein Großteil des Codes wurde umgeschrieben, um eine bessere Ausgangslage für neue Funktionen zu bieten."));
				changeLog.add(new ChangeLogEntry("20.11.2020 - Version 1.0.0", "Die ***" + cmdPrefix + "sells*** Funktion wurde fertig implementiert und der Bot auf dem Discord Server \"Ameisen an die Macht!\" vorgestellt."));
				changeLog.add(new ChangeLogEntry("17.11.2020 - Version 0.0.1", "Antony wurde bei Discord registriert und erste Test-Funktionen wurden geschrieben."));
					
				// Build output
				EmbedBuilder eb = new EmbedBuilder().setTitle("***Antony - Changelog***")
						.setColor(Antony.getBaseColor())
						.setThumbnail(channel.getJDA().getSelfUser().getEffectiveAvatarUrl())
						.setDescription("Hier kannst du nachvollziehen, wie sich Antony weiterentwickelt hat.")
						.setFooter("Version " + Antony.getVersion());
				
				// Add changelog entries to embedded message
				for(ChangeLogEntry cle: changeLog) {
					eb.addField(cle.getTitle(), cle.getNotes(), false);
				}
				
				channel.sendMessage(eb.build()).queue();
				break;

			default:
				channel.sendMessage(getCommandList().build()).queue();
				break;
			}
		} else {
			channel.sendMessage(getCommandList().build()).queue();
		}

	}

	/**
	 * Function to get all commands inside an embedded message
	 * @return	commandList
	 * 			as EmbedBuilder
	 */
	private EmbedBuilder getCommandList() {
		
		List<BotCommand> botCommands = new ArrayList<BotCommand>();
		List<BotCommand> modBotCommands = new ArrayList<BotCommand>();
		List<BotCommand> adminBotCommands = new ArrayList<BotCommand>();
		
		//Commands for everyone
		BotCommand antony = new BotCommand("antony", "Zeigt diese Übersicht an.");
		BotCommand antonyChangelog = new BotCommand("antony changelog", "Zeigt den Changelog von Antony an.");
		BotCommand emergency = new BotCommand("emergency", "Soll eine Hilfestellungen bei Notfällen zur Verfügung stellen.", "milben");
		BotCommand giveawayEnd = new BotCommand("giveaway end", "Löst ein laufendes Giveaway auf und ermittelt die Gewinner.", "https://discord.com/channels/375031723601297409/605451097699647665/798303589763252224 :tada:");
		BotCommand map = new BotCommand("map","Zeigt eine Karte von https://antmaps.org, auf der zu sehen ist, wo auf der Welt die gesuchte Ameisenart vorkommt.", "Lasius niger");
		BotCommand notify = new BotCommand("notify", "Kann genutzt werden, um über neue Einträge in Kanälen informiert zu werden. Nutze den Befehl, um detaillierte Informationen zur Handhabung zu bekommen.", "#kanal1");
		BotCommand pnlink = new BotCommand("pnlink", "Gibt einen formatierten Text für einen Kanal aus, der z.B. in PNs genutzt werden kann, um Kanäle zu verlinken, was über # nicht möglich ist. (Das letzte Leerzeichen der Ausgabe muss entfernt werden)", "#kanal1");
		BotCommand sells = new BotCommand("sells", "Listet zu der gesuchten Ameisenart alle Shops und zugehörigen Preise. Die Shops werden nach Namen sortiert ausgegeben. Die Daten werden von https://antcheck.info zur Verfügung gestellt. Vielen Dank hierfür!", "Lasius niger");
		BotCommand shopping = new BotCommand("shopping", "Zeigt eine Liste mit Kaufempfehlungen für die Ameisenhaltung.");
		BotCommand showAvatar = new BotCommand("showavatar", "Zeigt eine vergrößerte Version des Avatars/Profilbildes eines Benutzers.", "Antony");
		BotCommand userinfo = new BotCommand("userinfo", "Zeigt Details über den Benutzer.", "Antony");
		botCommands.add(antony);
		botCommands.add(antonyChangelog);
		botCommands.add(emergency);
		botCommands.add(giveawayEnd);
		botCommands.add(map);
		botCommands.add(notify);
		botCommands.add(pnlink);
		botCommands.add(sells);
		botCommands.add(shopping);
		botCommands.add(showAvatar);
		botCommands.add(userinfo);
		
		//Commands for mods
		BotCommand softban = new BotCommand("softban", "Funktion zur Verwaltung von Usern, deren Inhalte direkt nach posten gelöscht werden sollen.", "add ID NAME");
		BotCommand watchlist = new BotCommand("watchlist", "Funktion zur Verwaltung von Begriffen, über die das Mod-Team bei Verwendung eine Benachrichtigung erhalten möchte.", "add Ameisengesicht");
		modBotCommands.add(softban);
		modBotCommands.add(watchlist);
		
		//Commands for admins
		BotCommand channel = new BotCommand("channel", "Funktion, die z.B. dafür genutzt werden kann, alle Kanäle auszugeben, in denen schon länger keine Inhalte mehr gepostet wurden.", "list abandoned");
		BotCommand shutdown = new BotCommand("shutdown", "Funktion zum stoppen des Bots");
		adminBotCommands.add(channel);
		adminBotCommands.add(shutdown);
		
		
		EmbedBuilder eb = new EmbedBuilder().setTitle("***Antony***")
				.setColor(Antony.getBaseColor())
				.setDescription("Folgende Befehle können genutzt werden:")
				.setThumbnail(getChannel().getJDA().getSelfUser().getEffectiveAvatarUrl())
				.setFooter("Version " + Antony.getVersion());
		
		// add command descriptions for everyone to embedded output
		eb.addField("", ":green_circle: Funktionen, die jeder nutzen kann", false);
		eb = getEbDescriptionPart(eb, botCommands);
		
		// add mod command descriptions to embedded output
		eb.addField("", ":yellow_circle: Moderative Funktionen", false);
		eb = getEbDescriptionPart(eb, modBotCommands);
		
		// add admin command descriptions to embedded output
		eb.addField("", ":red_circle: Administrative Funktionen", false);
		eb = getEbDescriptionPart(eb, adminBotCommands);
		return eb;

	}
	
	private EmbedBuilder getEbDescriptionPart(EmbedBuilder eb, List<BotCommand> bcl) {
		for(BotCommand bc: bcl) {
			StringBuilder sb = new StringBuilder();
			sb.append(bc.getDescription());
			if(bc.getExample() != null) {
				sb.append("\n*Beispiel: " + Antony.getCmdPrefix() + bc.getCommand() + " " + bc.getExample() + "*");
			}
			eb.addField(Antony.getCmdPrefix() + bc.getCommand(), sb.toString(), false);
		}
		return eb;
	}

	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------

	public TextChannel getChannel() {
		return channel;
	}

	public void setChannel(TextChannel channel) {
		this.channel = channel;
	}

	
	// --------------------------------------------------
	// Additional Classes
	// --------------------------------------------------
	private class ChangeLogEntry {
	    private String title;
		private String notes;

	    ChangeLogEntry(String title, String notes) {
	        setTitle(title);
	        setNotes(notes);
	    }
	    
	    public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getNotes() {
			return notes;
		}

		public void setNotes(String notes) {
			this.notes = notes;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("ChangeLogEntry(");
			sb.append("Title: " + getTitle());
			sb.append(", Notes: " + getNotes());
			sb.append(")");
			return sb.toString();
		}
	}
	
	private class BotCommand {
	    private String command;
		private String description;
		private String example;

		BotCommand(String function, String description, String example) {
	        setCommand(function);
	        setDescription(description);
	        setExample(example);
	    }
		
		BotCommand(String function, String description) {
	        setCommand(function);
	        setDescription(description);
	    }

		public String getCommand() {
			return command;
		}

		public void setCommand(String function) {
			this.command = function;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getExample() {
			return example;
		}

		public void setExample(String example) {
			this.example = example;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("BotCommand(");
			sb.append("Function: " + getCommand());
			sb.append(", Description: " + getDescription());
			if(getExample() != null) {
				sb.append(", Example: " + getExample());
			}
			sb.append(")");
			return sb.toString();
		}
	    
	}
}