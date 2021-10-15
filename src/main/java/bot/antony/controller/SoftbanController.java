package bot.antony.controller;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.events.softban.UserDataSB;
import bot.antony.utils.Utils;

public class SoftbanController {
	private List<UserDataSB> bannedUser = new ArrayList<UserDataSB>();
	private String fileName;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public SoftbanController() {
		super();
		fileName = "antony.softbanneduser.json";
		initData();
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
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
		return Utils.storeJSONData(fileName, bannedUser);
	}
	
	@SuppressWarnings("unchecked")
	public void initData() {
		this.bannedUser = (List<UserDataSB>) Utils.loadJSONData(fileName, new TypeReference<List<UserDataSB>>(){}, bannedUser);
	}

	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public List<UserDataSB> getBannedUser() {
		return bannedUser;
	}

	public void setBannedUser(List<UserDataSB> bannedUser) {
		this.bannedUser = bannedUser;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String filename) {
		this.fileName = filename;
	}
	
	
	
}
