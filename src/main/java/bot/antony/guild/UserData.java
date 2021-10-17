package bot.antony.guild;

import java.util.HashMap;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

/**
 * Class to store user data
 */
public class UserData {
	private String id;
	private String name;
	private HashMap<Long, String> nicknames = new HashMap<Long, String>();
	private HashMap<Long, String> names = new HashMap<Long, String>();
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
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public void update(User user) {
		setName(user.getName());
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

	public HashMap<Long, String> getNicknames() {
		return nicknames;
	}

	public void setNicknames(HashMap<Long, String> nicknames) {
		this.nicknames = nicknames;
	}

	public HashMap<Long, String> getNames() {
		return names;
	}

	public void setNames(HashMap<Long, String> names) {
		this.names = names;
	}

	public Long getLastOnline() {
		return lastOnline;
	}

	public void setLastOnline(Long lastOnline) {
		this.lastOnline = lastOnline;
	}
}
