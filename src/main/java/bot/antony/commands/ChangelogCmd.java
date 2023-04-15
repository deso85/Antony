package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class ChangelogCmd extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ChangelogCmd() {
		super();
		this.privileged = false;
		this.name = "changelog";
		this.description = "Mit diesem Befehl lassen sich die letzten Einträge von Antonys Changelog einsehen.";
		this.shortDescription = "Zeigt die letzten Changelog-Einträge.";
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		EmbedBuilder eb = new EmbedBuilder().setTitle("***Antony - Changelog***")
				.setColor(Antony.getBaseColor())
				.setThumbnail(channel.getJDA().getSelfUser().getEffectiveAvatarUrl())
				.setDescription("Hier kannst du nachvollziehen, wie sich Antony zuletzt weiterentwickelt hat.")
				.setFooter("Version " + Antony.getVersion());
		
		for(ChangeLogEntry cle: getChangeLog(10)) {
			eb.addField(cle.getTitle(), cle.getNotes(), false);
		}
		channel.sendMessageEmbeds(eb.build()).queue();
	}
	
	private List<ChangeLogEntry> getChangeLog(int limit) {
		String cmdPrefix = Antony.getCmdPrefix();
		List<ChangeLogEntry> changeLog = new ArrayList<ChangeLogEntry>();
		changeLog.add(new ChangeLogEntry("15.04.2023 - Version 7.8.1", "Die ***" + cmdPrefix + "userinfo*** Funktion wurde auf das aktuelle Berechtigungssysten portiert. Vorbereitung einer zusätzlichen Sortierfunktion für Kanäle wurde implementiert."));
		changeLog.add(new ChangeLogEntry("15.04.2023 - Version 7.8.0", "Antony wurde so umgebaut, dass Commands nun auch im Forum und in Threads genutzt werden können."));
		changeLog.add(new ChangeLogEntry("11.04.2023 - Version 7.7.6", "Bugfix-Release: Der ***" + cmdPrefix + "sells*** Befehl gibt nun auch Ergebnisse zurück, wenn das Zeichenlimit von Discord überschritten wird."));
		changeLog.add(new ChangeLogEntry("05.04.2023 - Version 7.7.5", "Bugfix-Release: Es wurden ungewollt alle Kanäle sortiert, wenn ein Kanal verschoben wurde. Das passiert nun nicht mehr."));
		changeLog.add(new ChangeLogEntry("04.04.2023 - Version 7.7.4", "Kanäle lassen sich jetzt über den Befehl ***" + cmdPrefix + "channel*** in andere Kategorien verschieben und mit dieser synchronisieren."));
		changeLog.add(new ChangeLogEntry("23.03.2023 - Version 7.7.3", "Verwendete Programmbibliotheken aktualisiert. ***" + cmdPrefix + "notify*** wurde auf das aktuelle Berechtigungssystem portiert. Die Berechtigungsprüfung wurde vorgelagert, um dessen Ausführung sicherzustellen."));
		changeLog.add(new ChangeLogEntry("06.01.2023 - Version 7.7.2", "Wenn ein Haltungsbericht geschlossen bzw. pausiert wurde, konnte es vorkommen, dass der Verfasser des HBs wegen ausbleibender Updates angeschrieben wurde, wenn er zuvor auch schon erinnert wurde. Dieser Fehler wurde behoben. Zudem wurden die Artikel aus dem ***" + cmdPrefix + "shopping*** Befehl kontrolliert und teilweise ausgetauscht."));
		changeLog.add(new ChangeLogEntry("11.12.2022 - Version 7.7.1", "User Avatare werden jetzt aus dem Server-Profil ausgelesen und angezeigt."));
		changeLog.add(new ChangeLogEntry("30.11.2022 - Version 7.7.0", "Die ***" + cmdPrefix + "reminder*** Funktion wurde hinzugefügt und kann nun genutzt werden, um sich in der Zukunft an etwas erinnern zu lassen."));
		changeLog.add(new ChangeLogEntry("15.11.2022 - Version 7.6.1", "Der Code der ***" + cmdPrefix + "giveaway*** Funktion wurde noch einmal umgeschrieben, um sie wartbarer zu machen und kleinere Fehler zu beheben. Es wurden ebenfalls ein paar Fehler der ***" + cmdPrefix + "addhb*** Funktion behoben."));
		changeLog.add(new ChangeLogEntry("11.11.2022 - Version 7.6.0", "Die ***" + cmdPrefix + "giveaway*** Funktion wurde vollständig überarbeitet. Der Bot startet und beendet nun die Giveaways automatisch."));
		changeLog.add(new ChangeLogEntry("25.10.2022 - Version 7.5.0", "Programmbibliotheken wurden aufgrund von Security-Schwachstellen aktualisiert. Notwendiges Refactoring wurde vorgenommen."));
		changeLog.add(new ChangeLogEntry("27.10.2022 - Version 7.4.2", "Es wurden Anpassungen an Nachrichten vorgenommen, um die Interaktion mit Antony zu erleichtern. Kleinere Fehlerbehebungen an den Ausgaben."));
		changeLog.add(new ChangeLogEntry("25.10.2022 - Version 7.4.1", "Programmbibliotheken wurden aufgrund von Security-Schwachstellen aktualisiert."));
		changeLog.add(new ChangeLogEntry("22.10.2022 - Version 7.4.0", "Programmbibliotheken aktualisiert und hierdurch notwendige Änderungen vorgenommen. Produktempfehlungen (***" + cmdPrefix + "shopping***) aktualisiert. Der ***" + cmdPrefix + "userinfo*** Befehl wurde so abgeändert, dass das Sonderzeichen \"|\" escaped und es somit nicht versehentlich zur Textformatierung genutzt wird."));
		changeLog.add(new ChangeLogEntry("06.09.2022 - Version 7.3.2", "AAM spezifisches Release: Korrekturen an der Kontrolle der Haltungsberichte vorgenommen."));
		changeLog.add(new ChangeLogEntry("27.08.2022 - Version 7.3.1", "Kaufempfehlungen aktualisiert."));
		changeLog.add(new ChangeLogEntry("18.08.2022 - Version 7.3.0", "Die zugrunde liegende Programmbibliothek JDA wurde auf den neusten Stand aktualisiert und sämtlicher Code daran angepasst."));
		changeLog.add(new ChangeLogEntry("18.08.2022 - Version 7.2.2", "AAM spezifisches Release: Der User wird über die Ablehnung einer HB-Erstellung nun automatisch per PN informiert, wenn er den Command ***" + cmdPrefix + "addhb*** hierfür verwendet hat. Derselbe Command gibt nun auch detailliertere Fehlermeldungen aus, wenn keine oder zu viele Ameisenarten zwecks Plausibilitätsprüfung gefunden wurden. Die HB-Überprüfungen finden nun nicht mehrfach hintereinander statt, wenn der Bot mal restartet wird."));
		changeLog.add(new ChangeLogEntry("16.08.2022 - Version 7.2.1", "AAM spezifisches Release: Korrektur an der Kontroller der Haltungsberichte vorgenommen. Die Kontrolle ist abgebrochen, wenn der Author nicht mehr auf dem Server gewesen ist."));
		changeLog.add(new ChangeLogEntry("16.08.2022 - Version 7.2.0", "User können nun via Reaction von der Teilnahme an Voice-Kanälen ausgeschlossen oder wieder zugelassen werden."));
		changeLog.add(new ChangeLogEntry("12.08.2022 - Version 7.1.0", "AAM spezifisches Release: Die Haltungsberichte werden nun durch Antony auf Updates überprüft und wenn diese ausbleiben, wird der User an ein Update erinnert. Die 🟨 Reaction kann nun dafür genutzt werden, die Rolle \"GELBE KARTE :(\" zu setzen oder zu entfernen."));
		changeLog.add(new ChangeLogEntry("29.07.2022 - Version 7.0.11", "Die Funktionen ***" + cmdPrefix + "map*** und ***" + cmdPrefix + "showavatar*** wurden auf das neue Berechtigungssystem portiert. Kleinere Fehler-Korrekturen."));
		changeLog.add(new ChangeLogEntry("16.07.2022 - Version 7.0.10", "Die ***" + cmdPrefix + "category*** sync Funktion wurde dahingehend geändert, dass nun auch die Voice-Kanäle synchronisiert werden und mit dem zusätzlichen Parameter `-o` bzw. `--owner` festgelegt werden kann, ob der letzte Verfasser in einem Kanal zum Kanal-Besitzer mit Sonderberechtigungen gemacht werden kann."));
		changeLog.add(new ChangeLogEntry("15.07.2022 - Version 7.0.9", "Die ***" + cmdPrefix + "shutdown***, ***" + cmdPrefix + "whitelist***, ***" + cmdPrefix + "watchlist*** und ***" + cmdPrefix + "blacklist*** Funktionen wurde auf das neue Berechtigungssystem portiert. Die ***" + cmdPrefix + "addhb*** Funktion ignoriert bei der Angabe der Kategorie nun die \"#\", falls diese genutzt wird und ist damit fehlerunanfälliger."));
		changeLog.add(new ChangeLogEntry("01.07.2022 - Version 7.0.8", "Die ***" + cmdPrefix + "channel*** Funktion wurde auf das neue Berechtigungssystem portiert."));
		changeLog.add(new ChangeLogEntry("28.06.2022 - Version 7.0.7", "Die ***" + cmdPrefix + "addhb*** Funktion wurde auf das neue Berechtigungssystem portiert und kann jetzt Haltungsberichte zu Gattungen (z.B.: Lasius sp.) und vermuteten Arten (z.B.: Lasius cf. niger) anlegen."));
		changeLog.add(new ChangeLogEntry("25.06.2022 - Version 7.0.6", "Die ***" + cmdPrefix + "guild*** Funktion wurde auf das neue Berechtigungssystem portiert."));
		changeLog.add(new ChangeLogEntry("17.06.2022 - Version 7.0.5", "Ein Fehler hat dafür gesorgt, dass der ***" + cmdPrefix + "emergency*** Befehl nicht zuverlässig funktioniert hat. Dieser wurde behoben."));
		changeLog.add(new ChangeLogEntry("14.06.2022 - Version 7.0.4", "Diverse Befehle auf das neue Berechtigungssystem portiert. Die ***" + cmdPrefix + "category*** Funktion wurde dahingehend repariert, dass die Berechtigungen von Kanälen sich nun auch wieder mit der Kategorie, in der sie sind, synchronisieren lassen. Manche Befehle können jetzt über einen Alias aufgerufen werden."));
		changeLog.add(new ChangeLogEntry("13.06.2022 - Version 7.0.3", "Die ***" + cmdPrefix + "emergency*** Funktion wurde auf das neue Berechtitungssystem portiert."));
		changeLog.add(new ChangeLogEntry("12.06.2022 - Version 7.0.2", "Die ***" + cmdPrefix + "archive*** Funktion wurde auf das neue Berechtitungssystem portiert."));
		changeLog.add(new ChangeLogEntry("10.06.2022 - Version 7.0.1", "Portierung einiger Funktionen auf das neue Berechitugngssystem. Es gibt zusätzlich zwei neue Befehle: ***" + cmdPrefix + "help*** und ***" + cmdPrefix + "changelog***, die Antony intuitiver machen sollen."));
		changeLog.add(new ChangeLogEntry("09.06.2022 - Version 7.0.0", "Antony hat ein neues Berechtigungssystem erhalten, mit dem man User und Rollen gezielt auf Funktionen berechtigen kann. Alle Reaction-Befehle wurden auf dieses System bereits portiert."));
		changeLog.add(new ChangeLogEntry("05.06.2022 - Version 6.0.2", "Antony überwacht nun auch Threads."));
		changeLog.add(new ChangeLogEntry("04.06.2022 - Version 6.0.1", "***" + cmdPrefix + "addhb*** hat nun kein Zeitlimit mehr, sondern reagiert auf zu viele falsche Eingaben mit einem Abbruch und meldet dies nun auch."));
		changeLog.add(new ChangeLogEntry("04.06.2022 - Version 6.0.0", "JDA Programmbibliothek auf Version 5 aktualisiert und Antony darauf angepasst. Kanal-Sortierung ignoriert nun -sp und -cf."));
		changeLog.add(new ChangeLogEntry("31.05.2022 - Version 5.7.2", "Einige kleinere Korrekturen und Code-Anpassungen."));
		changeLog.add(new ChangeLogEntry("28.05.2022 - Version 5.7.1", "***" + cmdPrefix + "addhb*** wurde um eine Abfrage erweitert und ein kleinerer Fehler ausgebessert."));
		changeLog.add(new ChangeLogEntry("27.05.2022 - Version 5.7.0", "AAM spezifisch: Haltungsberichte können nun mithilfe des ***" + cmdPrefix + "addhb*** Befehls angefragt werden"));
		changeLog.add(new ChangeLogEntry("27.05.2022 - Version 5.6.1", "Mods werden nun über neu gejointe User, deren Accounts neu sind, informiert. Kleinere Anpassung an der AAM spezifischen Funktion für Vorschläge. Sämtliche Parameter lassen sich jetzt über die Config-File ändern."));
		changeLog.add(new ChangeLogEntry("26.05.2022 - Version 5.6.0", "AAM spezifisch: Vorschläge können nun über Antony moderiert werden."));
		changeLog.add(new ChangeLogEntry("20.05.2022 - Version 5.5.0", "Antony kann nun mit einer separaten Config-File betrieben werden. Einige Properties wurden zusätzlich angepasst."));
		changeLog.add(new ChangeLogEntry("19.05.2022 - Version 5.4.1", "Neue Kanäle können nun so angelegt werden, dass die zugehörige Kategorie direkt im Anschluss sortiert wird."));
		changeLog.add(new ChangeLogEntry("17.05.2022 - Version 5.4.0", "Voice- und Reaction-Aktivitäten sorgen jetzt dafür, dass Antony die Anwesenheit des Users wahrnimmt."));
		changeLog.add(new ChangeLogEntry("10.05.2022 - Version 5.3.5", "***" + cmdPrefix + "category*** sync hinzugefügt, um die Berechtigungen zu synchronisieren."));
		changeLog.add(new ChangeLogEntry("22.03.2022 - Version 5.3.4", "API URL für antcheck.info geändert, um die Funktionalität für z.B. den ***" + cmdPrefix + "sells*** Befehl wieder herzustellen."));
		changeLog.add(new ChangeLogEntry("12.03.2022 - Version 5.3.3", "Die Zeitzonen wurden geprüft und aktualisiert, damit die Ausgabe korrekt ist."));
		changeLog.add(new ChangeLogEntry("10.03.2022 - Version 5.3.2", "Ein kleiner Fehlerteufel wurde aus einem Text heraus exorziert. 😈"));
		changeLog.add(new ChangeLogEntry("25.02.2022 - Version 5.3.1", "Die Sortierfunktion für Kategorien ist nun nicht mehr Case sensitiv und nach Anlage eines neuen Kanals gibt Antony jetzt Feedback im jeweiligen Kanal."));
		changeLog.add(new ChangeLogEntry("25.02.2022 - Version 5.3.0", "***" + cmdPrefix + "channel*** kann nun dafür genutzt werden, neue Kanäle anzulegen."));
		changeLog.add(new ChangeLogEntry("18.02.2022 - Version 5.2.1", "Mods werden nun nicht mehr durch die 🔨 Reaction soft gebannt."));
		changeLog.add(new ChangeLogEntry("12.02.2022 - Version 5.2.0", "***" + cmdPrefix + "category*** wurde als neue administrative Funktion hinzugefügt."));
		changeLog.add(new ChangeLogEntry("28.12.2021 - Version 5.1.1", "Es wurde ein Artikel für die Kaufempfehlungen hinzugefügt und es wurden Fehler behoben, die durch Nachrichten in privaten Kanälen entstanden sind."));
		changeLog.add(new ChangeLogEntry("07.11.2021 - Version 5.1.0", "Nachrichten werden nun auch ausgewertet, wenn sie bearbeitet werden."));
		changeLog.add(new ChangeLogEntry("06.11.2021 - Version 5.0.0", "***" + cmdPrefix + "guild*** kann jetzt genutzt werden, um moderative und administrative Rollen zu konfigurieren. Darüber hinaus wurde das Berechtigungskonzept vollständig überarbeitet."));
		changeLog.add(new ChangeLogEntry("29.10.2021 - Version 4.2.0", "***" + cmdPrefix + "archive*** gibt nun ein formatiertes HTML-Dokument anstelle einer Text-Datei zurück."));
		changeLog.add(new ChangeLogEntry("27.10.2021 - Version 4.1.0", "***" + cmdPrefix + "archive*** wurde hinzugefügt und bietet eine neue moderative Funktion, mit der sich Kanalinhalte herunterladen lassen."));
		changeLog.add(new ChangeLogEntry("23.10.2021 - Version 4.0.0", "***" + cmdPrefix + "guild*** wurde hinzugefügt und bietet künftig Funktionen zur Administration des Discord Servers."));
		changeLog.add(new ChangeLogEntry("19.10.2021 - Version 3.5.0", "***" + cmdPrefix + "userinfo*** gibt historische Daten nun anders formatiert aus. Der zugrunde liegende Quellcode wurde überarbeitet. Der Befehl ***" + cmdPrefix + "user*** wurde hinzugefügt, über den künftig weitere moderative Funktionen bereitgestellt werden."));
		changeLog.add(new ChangeLogEntry("17.10.2021 - Version 3.4.1", "***" + cmdPrefix + "userinfo*** zeigt nun auch alle bekannten Namen an, gibt aber keine Elemente mehr aus, die keine Inhalte haben."));
		changeLog.add(new ChangeLogEntry("17.10.2021 - Version 3.4.0", "***" + cmdPrefix + "userinfo*** zeigt nun u.a. auch ältere bekannte Nicknames des User."));
		changeLog.add(new ChangeLogEntry("15.10.2021 - Version 3.3.1", "Kleinere Code-Änderungen, die der Übersichtlichkeit dienen."));
		changeLog.add(new ChangeLogEntry("13.10.2021 - Version 3.3.0", "***" + cmdPrefix + "emergency*** wurde so umgeschrieben, dass die Daten nicht mehr Teil des Quelltextes sind und nun dynamisch geladen werden. Antony muss künftig nicht mehr aktualisiert werden, wenn die Inhalte verändert werden."));
		changeLog.add(new ChangeLogEntry("13.10.2021 - Version 3.2.0", "Es wurden einige Verbesserungen am Quelltext vorgenommen."));
		changeLog.add(new ChangeLogEntry("13.10.2021 - Version 3.1.0", "***" + cmdPrefix + "softban*** wurde angepasst. Die 🔨 Reaction wurde an das neue Reaction-Handling angepasst und kann nun auch zum entbannen verwendet werden."));
		changeLog.add(new ChangeLogEntry("08.10.2021 - Version 3.0.0", "Das Reaction-Handling wurde komplett überarbeitet, einige Fehler ausgebessert und es gibt ein paar Änderungen beim Speichern und Laden von Daten."));
		changeLog.add(new ChangeLogEntry("04.10.2021 - Version 2.9.1", "Fehlerbehebungen für einzelne Funktionen (Downgrade von Libraries)."));
		changeLog.add(new ChangeLogEntry("04.10.2021 - Version 2.9.0", "***" + cmdPrefix + "whitelist*** wurde für Moderatoren hinzugefügt. Einige Klassen und Funktionen wurden refactored."));
		changeLog.add(new ChangeLogEntry("04.10.2021 - Version 2.8.0", "***" + cmdPrefix + "blacklist*** wurde für Moderatoren hinzugefügt, um schadhaften Content direkt herausfiltern zu lassen."));
		changeLog.add(new ChangeLogEntry("29.09.2021 - Version 2.7.1", "***" + cmdPrefix + "emergency schimmel*** bietet nun auch eine kurze Info zu Schimmel."));
		changeLog.add(new ChangeLogEntry("24.09.2021 - Version 2.7.0", "***" + cmdPrefix + "showavatar*** kann nun auch mit der User ID aufgerufen werden. Zudem kann der Avatar nun auch von Moderatoren über eine Reaction abgerufen werden."));
		changeLog.add(new ChangeLogEntry("31.08.2021 - Version 2.6.7", "Antony teilt nun allen mit, wenn jemand den Server verlassen hat."));
		changeLog.add(new ChangeLogEntry("28.08.2021 - Version 2.6.6", "Antony heißt nun alle neuen User auf dem Server willkommen."));
		changeLog.add(new ChangeLogEntry("14.08.2021 - Version 2.6.5", "Die *Rote Flagge* Reaction löscht Nachrichten nur noch, wenn nicht zu viele Löschungen in letzter Zeit vorgenommen wurden."));
		changeLog.add(new ChangeLogEntry("12.08.2021 - Version 2.6.4", "***" + cmdPrefix + "userinfo light*** gibt nun noch einen Nickname aus, sofern vorhanden."));
		changeLog.add(new ChangeLogEntry("27.07.2021 - Version 2.6.3", "***" + cmdPrefix + "sells*** wurde so überarbeitet, dass nun auch größere Datenmengen ausgegeben werden können."));
		changeLog.add(new ChangeLogEntry("12.07.2021 - Version 2.6.2", "Die Watchlist wird nun gegen eine Whitelist geprüft, um Meldungen zu reduzieren, die nicht relevant sind."));
		changeLog.add(new ChangeLogEntry("07.07.2021 - Version 2.6.1", "Weitere Willkommens-Nachricht hinzugefügt."));
		changeLog.add(new ChangeLogEntry("13.06.2021 - Version 2.6.0", "Sie Funktion ***" + cmdPrefix + "serverstats***, mit der Serverstatistiken ausgegeben werden können, wurde implementiert."));
		changeLog.add(new ChangeLogEntry("22.05.2021 - Version 2.5.4", "***" + cmdPrefix + "userinfo*** kann nun auch mit der ID des Benutzers verwendet werden."));
		changeLog.add(new ChangeLogEntry("05.05.2021 - Version 2.5.3b", "Bugfix: Aufgrund konkurrierender Zugriffe auf die Liste der zu benachrichtigenden User wurde der Code angepasst."));
		changeLog.add(new ChangeLogEntry("01.05.2021 - Version 2.5.2b", "Sollten mehr als 4 Flaggen unter einer Nachricht als Reaction gesetzt werden, wird diese Nachricht automatisch gelöscht."));
		changeLog.add(new ChangeLogEntry("01.05.2021 - Version 2.5.1b", "Bugfix: Der Abgleich von Usern, die einen Softbann haben, hat nicht richtig funktioniert. Dieser Fehler wurde behoben."));
		changeLog.add(new ChangeLogEntry("26.04.2021 - Version 2.5.0b", "Admins können nun über eine Reaction User auf dem Server freischalten."));
		changeLog.add(new ChangeLogEntry("15.04.2021 - Version 2.4.3b", "Bugfix für die ***" + cmdPrefix + "map*** Funktion."));
		changeLog.add(new ChangeLogEntry("02.04.2021 - Version 2.4.2b", "Kleineres Code-Refactoring zur Vereinheitlichung von Funktionen."));
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
		
		if(limit > 0) {
			return changeLog.stream().limit(limit).collect(Collectors.toList());
		} else {
			return changeLog;
		}
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------


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
	
}