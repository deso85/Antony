package bot.antony.events.softban;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

/**
 * Class to store user data
 */
public class UserDataSB {
	private String id;
	private String name;
	
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public UserDataSB() {
		super();
	}
	
	public UserDataSB(String id) {
		setId(id);
		setName(id);
	}

	public UserDataSB(String id, String name) {
		setId(id);
		setName(name);
	}
	
	public UserDataSB(User user) {
		setId(user.getId());
		setName(user.getName());
	}
	
	public UserDataSB(Member member) {
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
		if(!(o instanceof UserDataSB)) {
			return false;
		}
		// Typecast o to UserData so that we can compare data
		UserDataSB userData = (UserDataSB) o;
		// Compare the ID, because it's unique while the user name could have been changed
		if(getId().equals(userData.getId()) && getName().equals(userData.getName())) {
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
}
