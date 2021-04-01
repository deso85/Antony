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
	String filename;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public WatchlistController() {
		super();
		filename = "antony.watchlist.json";
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
			objectMapper.writeValue(new File(filename), watchlist);
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
		File file = new File(filename);
		if(file.exists() && !file.isDirectory()) { 
			this.watchlist = objectMapper.readValue(file, new TypeReference<List<String>>(){});
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if(watchlist.size() > 0) {
			int counter = 1;
			for(String word: watchlist) {
				sb.append(word);
				if(counter < watchlist.size()) {
					sb.append(", ");
				}
				counter++;
			}
		}else {
			sb.append("Watchlist is empty.");
		}
		
		return sb.toString();
	}
	
	public void addString(String string) {
		if(!watchlist.contains(string)) {
			watchlist.add(string);
			persistData();
		}
	}
	
	public void removeString(String string) {
		if(watchlist.contains(string)) {
			watchlist.remove(string);
			persistData();
		}
	}
	
	public void clearList() {
		watchlist = new ArrayList<String>();
		persistData();
	}

	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public List<String> getWatchlist() {
		return watchlist;
	}
}
