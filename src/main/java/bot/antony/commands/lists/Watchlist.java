package bot.antony.commands.lists;

import bot.antony.Antony;
import bot.antony.controller.ListController;

public class Watchlist extends ListCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public Watchlist() {
		super();
		allowedRoles.add("Intermorphe");
		listName = "Watchlist";
	}

	@Override
	public ListController getInstance() {
		return Antony.getWatchlistController();
	}
	
}