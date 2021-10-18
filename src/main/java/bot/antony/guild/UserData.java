package bot.antony.guild;

import java.util.Map.Entry;
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
		if(member.getNickname() != null && member.getNickname() != "") {
			setNickname(member.getNickname());
		}
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
		Entry<Long, String> lastEntry = getNames().lastEntry();
		if(lastEntry != null) {
			return lastEntry.getValue();
		}
		return "";
	}
	
	@JsonIgnore
	public void setName(String name) {
		if(getName() != name) {
			addName(System.currentTimeMillis(), name);
		}
	}
	
	@JsonIgnore
	public String getNickname() {
		Entry<Long, String> lastEntry = getNicknames().lastEntry();
		if(lastEntry != null) {
			return lastEntry.getValue();
		}
		return "";
	}
	
	@JsonIgnore
	public void setNickname(String nickname) {
		if(getNickname() != nickname) {
			addNickname(System.currentTimeMillis(), nickname);
		}
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
