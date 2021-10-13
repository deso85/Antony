package bot.antony.controller;

public class WhiteListController extends ListController {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public WhiteListController() {
		super();
		fileName = "antony.whitelist.json";
		initData();
	}
}
