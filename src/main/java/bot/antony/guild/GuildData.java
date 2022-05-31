package bot.antony.guild;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Guild;

/**
 * Class to store Discord server data to help manage notifications
 */
public class GuildData {
	private String id;
	private String name;
	private long logChannelID;
	private long welcomeChannelID;
	private long activationRulesChannelID;
	private long exitChannelID;
	private List<String> adminRoles = new ArrayList<String>();
	private List<String> modRoles = new ArrayList<String>();

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public GuildData() {
		super();
	}
	
	public GuildData(String id) {
		setId(id);
		setName(id);
	}

	public GuildData(String id, String name) {
		setId(id);
		setName(name);
	}
	
	public GuildData(Guild guild) {
		setId(guild.getId());
		setName(guild.getName());
		setWelcomeChannelID(guild.getSystemChannel().getIdLong());
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public void update(Guild guild) {
		setName(guild.getName());
	}
	
	@Override
	public String toString() {
		return "id: " + getId() + ", name: " + getName();
	}
	
	@Override
	public boolean equals(Object o) {
		// If the object is compared with itself then return true
		if (o == this) {
			return true;
		}
		// Check if o is an instance of UserData or not "null instanceof [type]" also returns false
		if(!(o instanceof GuildData)) {
			return false;
		}
		// Typecast o to ChannelData so that we can compare data
		GuildData guildData = (GuildData) o;
		// Compare the ID, because it's unique while the guild name could have been changed
		if(getId().equals(guildData.getId())) {
			return true;
		}
		return false;
	}
	
	public boolean addAdminRole(String roleName) {
		if(!adminRoles.contains(roleName)) {
			adminRoles.add(roleName);
			return true;
		}
		return false;
	}
	
	public boolean removeAdminRole(String roleName) {
		if(adminRoles.contains(roleName)) {
			adminRoles.remove(roleName);
			return true;
		}
		return false;
	}
	
	public boolean addModRole(String roleName) {
		if(!modRoles.contains(roleName)) {
			modRoles.add(roleName);
			return true;
		}
		return false;
	}
	
	public boolean removeModRole(String roleName) {
		if(modRoles.contains(roleName)) {
			modRoles.remove(roleName);
			return true;
		}
		return false;
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getAdminRoles() {
		return adminRoles;
	}

	public void setAdminRoles(List<String> adminRoles) {
		this.adminRoles = adminRoles;
	}

	public List<String> getModRoles() {
		return modRoles;
	}

	public void setModRoles(List<String> modRoles) {
		this.modRoles = modRoles;
	}

	public long getLogChannelID() {
		return logChannelID;
	}

	public void setLogChannelID(long logChannelID) {
		this.logChannelID = logChannelID;
	}

	public long getWelcomeChannelID() {
		return welcomeChannelID;
	}

	public void setWelcomeChannelID(long welcomeChannelID) {
		this.welcomeChannelID = welcomeChannelID;
	}

	public long getActivationRulesChannelID() {
		return activationRulesChannelID;
	}

	public void setActivationRulesChannelID(long activationRulesChannelID) {
		this.activationRulesChannelID = activationRulesChannelID;
	}
	
	public long getExitChannelID() {
		return exitChannelID;
	}

	public void setExitChannelID(long exitChannelID) {
		this.exitChannelID = exitChannelID;
	}
}