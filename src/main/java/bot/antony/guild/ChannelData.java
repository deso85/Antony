package bot.antony.guild;

import net.dv8tion.jda.api.entities.channel.Channel;

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

	public ChannelData(Channel channel) {
		setId(channel.getId());
		setName(channel.getName());
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public void update(Channel channel) {
		setName(channel.getName());
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
		if(!(o instanceof ChannelData)) {
			return false;
		}
		// Typecast o to ChannelData so that we can compare data
		ChannelData channelData = (ChannelData) o;
		// Compare the ID, because it's unique while the channel name could have been changed
        return getId().equals(channelData.getId());
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