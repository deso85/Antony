package bot.antony.notifications;

/**
 * Class to store channel data to help manage the notifications
 */
public class ChannelData {
	private String id;
	private String name;
	
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ChannelData() {
		super();
	}
	
	public ChannelData(String id) {
		setId(id);
		setName(id);
	}

	public ChannelData(String id, String name) {
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