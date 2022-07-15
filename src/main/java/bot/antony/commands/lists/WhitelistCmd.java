package bot.antony.commands.lists;

import bot.antony.Antony;
import bot.antony.controller.ListController;

public class WhitelistCmd extends ListCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public WhitelistCmd() {
		super();
		listName = "Whitelist";
		this.name = "whitelist";
		this.description = "Mit diesem Befehl lässt sich die Whitelist verwalten. Sollten Inhalte der Whitelist in Beiträgen vorkommen, werden diese nicht durch andere Listen geprüft.";
		this.shortDescription = "Mit diesem Befehl lässt sich die Whitelist verwalten.";
		this.cmdParams.put("add textString", "Fügt der " + listName + " einen Text/Begriff hinzu.");
		this.cmdParams.put("remove textString", "Entfernt einen Text/Begriff von der " + listName + ".");
		this.cmdParams.put("list", "Gibt alle Einträge von der " + listName + " aus.");
		this.cmdParams.put("reload", "Lädt die " + listName + " neu in den Speicher.");
		this.cmdParams.put("clear", "Löscht die " + listName + ".");
	}
	
	@Override
	public ListController getInstance() {
		return Antony.getWhitelistController();
	}
	
}