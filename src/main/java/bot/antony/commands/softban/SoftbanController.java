package bot.antony.commands.softban;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import bot.antony.events.softban.UserDataSB;
import bot.antony.utils.Utils;

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
		return Utils.storeData(bannedUserFile, bannedUser);
	}
	
	@SuppressWarnings("unchecked")
	public void initData() throws JsonParseException, JsonMappingException, IOException{
		this.bannedUser = (List<UserDataSB>) Utils.loadData(bannedUserFile, new TypeReference<List<UserDataSB>>(){}, bannedUser);
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
