package bot.antony.guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private long commandsChannelID;
	private List<String> adminRoles = new ArrayList<String>();
	private List<String> modRoles = new ArrayList<String>();
	private Map<String, ArrayList<Long>> cmdRoles = new HashMap<String, ArrayList<Long>>();
	private Map<String, ArrayList<Long>> cmdMembers = new HashMap<String, ArrayList<Long>>();
	private Map<String, ArrayList<Long>> reactionRoles = new HashMap<String, ArrayList<Long>>();
	private Map<String, ArrayList<Long>> reactionMembers = new HashMap<String, ArrayList<Long>>();

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
	
	//Functions for command privileges based on roles
	public boolean addCmdRole(String cmdName, Long roleId) {
		ArrayList<Long> roles = new ArrayList<Long>();
		if(cmdRoles.containsKey(cmdName)) {
			roles = cmdRoles.get(cmdName);
			if(roles.contains(roleId)) {
				return false;
			} else {
				roles.add(roleId);
				cmdRoles.replace(cmdName, roles);
				return true;
			}
		} else {
			roles.add(roleId);
			cmdRoles.put(cmdName, roles);
			return true;
		}
	}
	
	public ArrayList<Long> getCmdRoles(String cmdName){
		return cmdRoles.get(cmdName);
	}
	
	public boolean removeCmdRole(String cmdName, Long roleId) {
		if(cmdRoles.containsKey(cmdName)) {
			ArrayList<Long> roles = cmdRoles.get(cmdName);
			if(roles.remove(roleId)) {
				cmdRoles.replace(cmdName, roles);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	//Functions for command privileges based on members
	public boolean addCmdMember(String cmdName, Long memberId) {
		ArrayList<Long> members = new ArrayList<Long>();
		if(cmdMembers.containsKey(cmdName)) {
			members = cmdMembers.get(cmdName);
			if(members.contains(memberId)) {
				return false;
			} else {
				members.add(memberId);
				cmdMembers.replace(cmdName, members);
				return true;
			}
		} else {
			members.add(memberId);
			cmdMembers.put(cmdName, members);
			return true;
		}
	}
	
	public ArrayList<Long> getCmdMembers(String cmdName){
		return cmdMembers.get(cmdName);
	}
	
	public boolean removeCmdMember(String cmdName, Long memberId) {
		if(cmdMembers.containsKey(cmdName)) {
			ArrayList<Long> members = cmdMembers.get(cmdName);
			if(members.remove(memberId)) {
				cmdMembers.replace(cmdName, members);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	//Functions for reaction privileges based on roles
	public boolean addReactionRole(String reactionName, Long roleId) {
		ArrayList<Long> roles = new ArrayList<Long>();
		if(reactionRoles.containsKey(reactionName)) {
			roles = reactionRoles.get(reactionName);
			if(roles.contains(roleId)) {
				return false;
			} else {
				roles.add(roleId);
				reactionRoles.replace(reactionName, roles);
				return true;
			}
		} else {
			roles.add(roleId);
			reactionRoles.put(reactionName, roles);
			return true;
		}
	}
	
	public ArrayList<Long> getReactionRoles(String reactionName){
		return reactionRoles.get(reactionName);
	}
	
	public boolean removeReactionRole(String reactionName, Long roleId) {
		if(reactionRoles.containsKey(reactionName)) {
			ArrayList<Long> roles = reactionRoles.get(reactionName);
			if(roles.remove(roleId)) {
				reactionRoles.replace(reactionName, roles);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	//Functions for reaction privileges based on members
	public boolean addReactionMember(String reactionName, Long memberId) {
		ArrayList<Long> members = new ArrayList<Long>();
		if(reactionMembers.containsKey(reactionName)) {
			members = reactionMembers.get(reactionName);
			if(members.contains(memberId)) {
				return false;
			} else {
				members.add(memberId);
				reactionMembers.replace(reactionName, members);
				return true;
			}
		} else {
			members.add(memberId);
			reactionMembers.put(reactionName, members);
			return true;
		}
	}
	
	public ArrayList<Long> getReactionMembers(String reactionName){
		return reactionMembers.get(reactionName);
	}
	
	public boolean removeReactionMember(String reactionName, Long memberId) {
		if(reactionMembers.containsKey(reactionName)) {
			ArrayList<Long> members = reactionMembers.get(reactionName);
			if(members.remove(memberId)) {
				reactionMembers.replace(reactionName, members);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
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

	public long getCommandsChannelID() {
		return commandsChannelID;
	}

	public void setCommandsChannelID(long commandsChannelID) {
		this.commandsChannelID = commandsChannelID;
	}

	public Map<String, ArrayList<Long>> getCmdRoles() {
		return cmdRoles;
	}

	public void setCmdRoles(Map<String, ArrayList<Long>> cmdRoles) {
		this.cmdRoles = cmdRoles;
	}

	public Map<String, ArrayList<Long>> getCmdMembers() {
		return cmdMembers;
	}

	public void setCmdMembers(Map<String, ArrayList<Long>> cmdMembers) {
		this.cmdMembers = cmdMembers;
	}

	public Map<String, ArrayList<Long>> getReactionRoles() {
		return reactionRoles;
	}

	public void setReactionRoles(Map<String, ArrayList<Long>> reactionRoles) {
		this.reactionRoles = reactionRoles;
	}

	public Map<String, ArrayList<Long>> getReactionMembers() {
		return reactionMembers;
	}

	public void setReactionMembers(Map<String, ArrayList<Long>> reactionMembers) {
		this.reactionMembers = reactionMembers;
	}
	
}