package bot.antony.commands.lists;

import bot.antony.Antony;
import bot.antony.controller.ListController;

public class WatchlistCmd extends ListCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public WatchlistCmd() {
		super();
		listName = "Watchlist";
		this.name = "watchlist";
		this.description = "Mit diesem Befehl lässt sich die Watchlist verwalten. Sollten Inhalte der Watchlist in Beiträgen vorkommen, werden diese den Moderatoren gemeldet.";
		this.shortDescription = "Mit diesem Befehl lässt sich die Watchlist verwalten.";
		this.cmdParams.put("add textString", "Fügt der " + listName + " einen Text/Begriff hinzu.");
		this.cmdParams.put("remove textString", "Entfernt einen Text/Begriff von der " + listName + ".");
		this.cmdParams.put("list", "Gibt alle Einträge von der " + listName + " aus.");
		this.cmdParams.put("reload", "Lädt die " + listName + " neu in den Speicher.");
		this.cmdParams.put("clear", "Löscht die " + listName + ".");
	}

	@Override
	public ListController getInstance() {
		return Antony.getWatchlistController();
	}
	
}