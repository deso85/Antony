package bot.antony.commands.lists;

import bot.antony.Antony;
import bot.antony.controller.ListController;

public class Whitelist extends ListCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public Whitelist() {
		super();
		listName = "Whitelist";
	}
	
	@Override
	public ListController getInstance() {
		return Antony.getWhitelistController();
	}
	
}