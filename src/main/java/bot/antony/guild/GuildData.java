package bot.antony.guild;

import net.dv8tion.jda.api.entities.Guild;

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
	
	public GuildData(Guild guild) {
		setId(guild.getId());
		setName(guild.getName());
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