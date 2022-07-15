package bot.antony.commands.lists;

import bot.antony.Antony;
import bot.antony.controller.ListController;

public class BlacklistCmd extends ListCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public BlacklistCmd() {
		super();
		listName = "Blacklist";
		this.name = "blacklist";
		this.description = "Mit diesem Befehl lässt sich die Blacklist verwalten. Sollten Inhalte der Blacklist in Beiträgen vorkommen, werden diese automatisch gelöscht.";
		this.shortDescription = "Mit diesem Befehl lässt sich die Blacklist verwalten.";
		this.cmdParams.put("add textString", "Fügt der " + listName + " einen Text/Begriff hinzu.");
		this.cmdParams.put("remove textString", "Entfernt einen Text/Begriff von der " + listName + ".");
		this.cmdParams.put("list", "Gibt alle Einträge von der " + listName + " aus.");
		this.cmdParams.put("reload", "Lädt die " + listName + " neu in den Speicher.");
		this.cmdParams.put("clear", "Löscht die " + listName + ".");
	}
	
	@Override
	public ListController getInstance() {
		return Antony.getBlacklistController();
	}
	
}