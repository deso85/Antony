package bot.antony.commands.lists;

import bot.antony.Antony;
import bot.antony.controller.ListController;

public class Blacklist extends ListCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public Blacklist() {
		super();
		listName = "Blacklist";
	}
	
	@Override
	public ListController getInstance() {
		return Antony.getBlacklistController();
	}
	
}