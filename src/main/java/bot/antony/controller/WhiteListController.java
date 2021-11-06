package bot.antony.controller;

import java.util.regex.Pattern;

public class WhiteListController extends ListController {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public WhiteListController() {
		super();
		fileName = "antony.whitelist.json";
		initData();
	}
	
	public String getCleanedMessage(String message) {
		String modifiedMessage = message;
		
		for(String string: getList()) {
			modifiedMessage = modifiedMessage.replaceAll("(?i)" + Pattern.quote(string), "");
		}
		
		return modifiedMessage;
	}
}
