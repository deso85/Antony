package bot.antony.controller;

public class BlackListController extends ListController {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public BlackListController() {
		super();
		fileName = "antony.blacklist.json";
		initData();
	}
}
