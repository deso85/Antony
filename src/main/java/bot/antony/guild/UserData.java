package bot.antony.guild;

import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

/**
 * Class to store user data
 */
public class UserData {
	private String id;
	private TreeMap<Long, String> names = new TreeMap<Long, String>();
	private TreeMap<Long, String> nicknames = new TreeMap<Long, String>();
	private Long lastOnline;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public UserData() {
		super();
	}
	
	public UserData(String id) {
		setId(id);
		setName(id);
	}

	public UserData(String id, String name) {
		setId(id);
		setName(name);
	}
	
	public UserData(User user) {
		setId(user.getId());
		setName(user.getName());
	}
	
	public UserData(Member member) {
		User user = member.getUser();
		setId(user.getId());
		setName(user.getName());
		setNickname(member.getNickname());
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
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
		if(!(o instanceof UserData)) {
			return false;
		}
		// Typecast o to UserData so that we can compare data
		UserData userData = (UserData) o;
		// Compare the ID, because it's unique while the user name could have been changed
		if(getId().equals(userData.getId())) {
			return true;
		}
		return false;
	}
	
	public void addNickname(Long time, String nick) {
		nicknames.put(time, nick);
	}
	
	public void addName(Long time, String name) {
		names.put(time, name);
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
	
	@JsonIgnore
	public String getName() {
		if(getNames().lastEntry() != null) {
			return getNames().lastEntry().getValue();
		}
		return "";
	}
	
	@JsonIgnore
	public boolean setName(String name) {
		if(!getName().equals(name)) {
			addName(System.currentTimeMillis(), name);
			return true;
		}
		return false;
	}
	
	@JsonIgnore
	public String getNickname() {
		if(getNicknames().lastEntry() != null) {
			return getNicknames().lastEntry().getValue();
		}
		return "";
	}
	
	@JsonIgnore
	public boolean setNickname(String nickname) {
		if(!getNickname().equals(nickname)) {
			addNickname(System.currentTimeMillis(), nickname);
			return true;
		}
		return false;
	}
	
	public TreeMap<Long, String> getNames() {
		return names;
	}

	public void setNames(TreeMap<Long, String> names) {
		this.names = names;
	}
	
	public TreeMap<Long, String> getNicknames() {
		return nicknames;
	}

	public void setNicknames(TreeMap<Long, String> nicknames) {
		this.nicknames = nicknames;
	}

	public Long getLastOnline() {
		return lastOnline;
	}

	public void setLastOnline(Long lastOnline) {
		this.lastOnline = lastOnline;
	}

}
