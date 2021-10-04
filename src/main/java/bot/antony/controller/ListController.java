package bot.antony.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bot.antony.Antony;

public class ListController {
	protected List<String> list = new ArrayList<String>();
	protected String fileName;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ListController() {
		super();
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
			objectMapper.writeValue(new File(fileName), list);
			return true;
			
		} catch (IOException e) {
			Antony.getLogger().error("Could not store list!", e);
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
		File file = new File(fileName);
		if(file.exists() && !file.isDirectory()) { 
			this.list = objectMapper.readValue(file, new TypeReference<List<String>>(){});
		}
	}
	
	public String list() {
		StringBuilder sb = new StringBuilder();
			
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
			sb.append("Die Liste ist leer.");
		}
		
		return sb.toString();
	}
	
	public boolean add(String string) {
		if(!list.contains(string)) {
			list.add(string);
			list.sort(String.CASE_INSENSITIVE_ORDER);
			persistData();
			return true;
		}
		return false;
	}
	
	public boolean remove(String string) {
		if(list.contains(string)) {
			list.remove(string);
			persistData();
			return true;
		}
		return false;
	}
	
	public void clear() {
		list = new ArrayList<String>();
		persistData();
	}
	
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public List<String> getList() {
		return list;
	}
	
	public String getFileName() {
		return this.fileName;
	}
}
