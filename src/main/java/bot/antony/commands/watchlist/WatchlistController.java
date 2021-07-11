package bot.antony.commands.watchlist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bot.antony.Antony;

public class WatchlistController {
	List<String> watchlist = new ArrayList<String>();
	List<String> whitelist = new ArrayList<String>();
	String watchlistFilename;
	String whitelistFilename;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public WatchlistController() {
		super();
		watchlistFilename = "antony.watchlist.json";
		whitelistFilename = "antony.watchlist.whitelist.json";
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	/**
	 * Persists data in JSON format
	 * @return	TRUE if data has been stored or FALSE if not
	 */
	public boolean persistData() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(new File(watchlistFilename), watchlist);
			objectMapper.writeValue(new File(whitelistFilename), whitelist);
			return true;
			
		} catch (IOException e) {
			Antony.getLogger().error("Could not store watchlist!", e);
		}
		return false;
	}
	
	/**
	 * Loads data in JSON format from stored file
	 * @throws	JsonParseException
	 * @throws	JsonMappingException
	 * @throws	IOException
	 */
	public void initData() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		File file = new File(watchlistFilename);
		if(file.exists() && !file.isDirectory()) { 
			this.watchlist = objectMapper.readValue(file, new TypeReference<List<String>>(){});
		}
		file = new File(whitelistFilename);
		if(file.exists() && !file.isDirectory()) { 
			this.whitelist = objectMapper.readValue(file, new TypeReference<List<String>>(){});
		}
	}
	
	public String list() {
		return list("watchlist");
	}
	
	public String list(String listname) {
		StringBuilder sb = new StringBuilder();
		List<String> list = watchlist;
		if(listname.toLowerCase().equals("whitelist")) {
			list = whitelist;
		}
			
		if(list.size() > 0) {
			int counter = 1;
			for(String word: list) {
				sb.append(word);
				if(counter < list.size()) {
					sb.append(", ");
				}
				counter++;
			}
		}else {
			sb.append("List is empty.");
		}
		
		return sb.toString();
	}
	
	public void addWatchlistEntry(String string) {
		if(!watchlist.contains(string)) {
			watchlist.add(string);
			persistData();
		}
		watchlist.sort(String.CASE_INSENSITIVE_ORDER);
	}
	
	public void addWhitelistEntry(String string) {
		if(!whitelist.contains(string)) {
			whitelist.add(string);
			persistData();
		}
		whitelist.sort(String.CASE_INSENSITIVE_ORDER);
	}
	
	public void removeWatchlistEntry(String string) {
		if(watchlist.contains(string)) {
			watchlist.remove(string);
			persistData();
		}
	}
	
	public void removeWhitelistEntry(String string) {
		if(whitelist.contains(string)) {
			whitelist.remove(string);
			persistData();
		}
	}
	
	public void clearWatchlist() {
		watchlist = new ArrayList<String>();
		persistData();
	}
	
	public void clearWhitelist() {
		whitelist = new ArrayList<String>();
		persistData();
	}

	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public List<String> getWatchlist() {
		return watchlist;
	}
	
	public List<String> getWhitelist() {
		return whitelist;
	}
}
