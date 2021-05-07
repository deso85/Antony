package bot.antony.commands.softban;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bot.antony.Antony;
import bot.antony.events.softban.UserDataSB;

public class SoftbanController {
	private List<UserDataSB> bannedUser = new ArrayList<UserDataSB>();
	private String bannedUserFile = "antony.softbanneduser.json";
	
	public boolean ban(UserDataSB user) {
		if(!bannedUser.contains(user)) {
			bannedUser.add(user);
			persistData();
			return true;
		}
		return false;
	}
	
	public boolean unban(UserDataSB user) {
		if(bannedUser.contains(user)) {
			bannedUser.remove(user);
			persistData();
			return true;
		}
		return false;
	}
	
	public boolean persistData() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(new File(bannedUserFile), bannedUser);	//Map of softbanned user
			return true;
			
		} catch (IOException e) {
			Antony.getLogger().error("Could not store softbanned user data!", e);
		}
		return false;
	}
	
	public void initData() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		File file = new File(bannedUserFile);	//Map of softbanned user
		if(file.exists() && !file.isDirectory()) { 
			this.bannedUser = objectMapper.readValue(file, new TypeReference<List<UserDataSB>>(){});
		}
	}

	public List<UserDataSB> getBannedUser() {
		return bannedUser;
	}

	public void setBannedUser(List<UserDataSB> bannedUser) {
		this.bannedUser = bannedUser;
	}

	public String getBannedUserFile() {
		return bannedUserFile;
	}

	public void setBannedUserFile(String bannedUserFile) {
		this.bannedUserFile = bannedUserFile;
	}
	
	
	
}
