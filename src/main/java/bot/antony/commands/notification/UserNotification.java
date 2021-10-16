package bot.antony.commands.notification;

import java.util.ArrayList;

import bot.antony.guild.ChannelData;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;

public class UserNotification {
	UserData user;
	GuildData guild;
	ArrayList<ChannelData> channels = new ArrayList<ChannelData>();
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public UserNotification() {
		super();
	}
	
	public UserNotification(UserData user, GuildData guild) {
		setUser(user);
		setGuild(guild);
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public boolean addChannel(ChannelData channel) {
		if(!getChannels().contains(channel)) {
			return getChannels().add(channel);
		}
		return false;
	}
	
	@Override
	public boolean equals(Object o) {
		// If the object is compared with itself then return true
		if (o == this) {
			return true;
		}
		// Check if o is an instance of UserNotification or not "null instanceof [type]" also returns false
		if(!(o instanceof UserNotification)) {
			return false;
		}
		// Typecast o to ChannelData so that we can compare data
		UserNotification userNotification = (UserNotification) o;
		// Compare the UserData object, because it's unique
		if(getUser().equals(userNotification.getUser()) && getGuild().equals(userNotification.getGuild())) {
			return true;
		}
		return false;
	}
	
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
		this.user = user;
	}

	public GuildData getGuild() {
		return guild;
	}

	public void setGuild(GuildData guild) {
		this.guild = guild;
	}

	public ArrayList<ChannelData> getChannels() {
		return channels;
	}

	public void setChannels(ArrayList<ChannelData> channels) {
		this.channels = channels;
	}
	
}
