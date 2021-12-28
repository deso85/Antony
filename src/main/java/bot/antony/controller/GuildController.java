package bot.antony.controller;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.guild.GuildData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class GuildController {

	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public GuildController() {
		super();
	}
	

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public TextChannel getLogChannel(Guild guild) {
		GuildData guildData = loadGuildData(guild);
		return guild.getTextChannelById(guildData.getLogChannelID());
	}
	
	public TextChannel getWelcomeChannel(Guild guild) {
		GuildData guildData = loadGuildData(guild);
		if(guild.getTextChannelById(guildData.getWelcomeChannelID()) != null) {
			return guild.getTextChannelById(guildData.getWelcomeChannelID());
		}
		return guild.getSystemChannel();
	}
	
	public List<String> getAdminRoles(Guild guild){
		GuildData guildData = loadGuildData(guild);
		return guildData.getAdminRoles();
	}
	
	public List<String> getModRoles(Guild guild){
		GuildData guildData = loadGuildData(guild);
		return guildData.getModRoles();
	}
	
	public String getStoragePath(Guild guild) {
		return Antony.getDataPath() + "guilds" + File.separator + guild.getId() + " - " + guild.getName();
	}
	
	public void changeGuildName(Guild guild, String oldName, String newName) {
		// Rename directory
		String oldDir = Antony.getDataPath() + "guilds" + File.separator + guild.getId() + " - " + oldName;
		String newDir = Antony.getDataPath() + "guilds" + File.separator + guild.getId() + " - " + newName;
		
		File directory = new File(oldDir);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }
	    
	    File sourceFile = new File(oldDir);
		File destFile = new File(newDir);
		
		if (sourceFile.renameTo(destFile)) {
			Antony.getLogger().info("Guild name changed from \"" + oldName + "\" to \"" + newName + "\" - Updated directory name.");
		} else {
			Antony.getLogger().error("Guild name changed from \"" + oldName + "\" to \"" + newName + "\" - Wasn't able to update directory name.");
		}
		
		// Update name
		GuildData guildData = loadGuildData(guild);
		guildData.setName(newName);
		saveGuildData(guildData, guild);
	}
	
	public GuildData loadGuildData(Guild guild) {
		GuildData guildData = new GuildData();
		String subfolder = "guilds" + File.separator + guild.getId() + " - " + guild.getName() + File.separator;
		String fileName = guild.getId() + ".json";
		
		//Load guild data if exists
		guildData = (GuildData) Utils.loadJSONData(subfolder, fileName, new TypeReference<GuildData>(){}, guildData);
		
		//If guild is unknown
		if(guildData.getId() == null || guildData.getId().equals("")) {
			guildData = new GuildData(guild);
			saveGuildData(guildData, guild);
		}
		
		return guildData;
	}
	
	public void saveGuildData(GuildData guildData, Guild guild) {
		String subfolder = "guilds" + File.separator + guild.getId() + " - " + guild.getName() + File.separator;
		String fileName = guild.getId() + ".json";
		
		Utils.saveJSONData(subfolder, fileName, guildData);
	}
	
	public boolean memberIsAdmin(Member member) {
		if(member == null) {
			return false;
		}
			
		GuildData guildData = loadGuildData(member.getGuild());
		if(member.isOwner()) {
			return true;
		}
		
		if(member.hasPermission(Permission.ADMINISTRATOR)) {
			return true;
		}
		
		for(Role memRole : member.getRoles()) {
			if(guildData.getAdminRoles().contains(memRole.getName())) {
				return true;
			}
		}

		return false;
	}
	
	public boolean memberIsMod(Member member) {
		if(member == null) {
			return false;
		}
		
		//Admins are mods
		GuildData guildData = loadGuildData(member.getGuild());
		if(memberIsAdmin(member)) {
			return true;
		}
		
		for(Role memRole : member.getRoles()) {
			if(guildData.getModRoles().contains(memRole.getName())) {
				return true;
			}
		}

		return false;	
	}
	
	public boolean isLogChannel(TextChannel channel) {
		if(channel == getLogChannel(channel.getGuild())) {
			return true;
		}
		return false;
	}
}
