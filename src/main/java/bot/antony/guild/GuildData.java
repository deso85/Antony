package bot.antony.guild;

/**
 * Class to store Discord server data to help manage notifications
 */
public class GuildData {
	private String id;
	private String name;


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
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public String toString() {
		return "id:" + getId() + ", name:" + getName();
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