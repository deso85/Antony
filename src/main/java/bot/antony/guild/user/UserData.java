package bot.antony.guild.user;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

/**
 * Class to store user data
 */
public class UserData {
	private String id;
	private String name;
	
	
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
