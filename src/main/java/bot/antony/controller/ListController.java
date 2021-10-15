package bot.antony.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import bot.antony.utils.Utils;

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
		return Utils.storeJSONData(fileName, this.list);
	}
	
	/**
	 * Loads data in JSON format from stored file
	 * @throws	JsonParseException
	 * @throws	JsonMappingException
	 * @throws	IOException
	 */
	@SuppressWarnings("unchecked")
	public void initData() {
		this.list = (List<String>) Utils.loadJSONData(fileName, new TypeReference<List<String>>(){}, this.list);
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
			return persistData();
		}
		return false;
	}
	
	public boolean remove(String string) {
		if(list.contains(string)) {
			list.remove(string);
			return persistData();
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
