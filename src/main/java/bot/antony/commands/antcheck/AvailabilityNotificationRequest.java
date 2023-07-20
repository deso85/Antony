package bot.antony.commands.antcheck;

import java.util.ArrayList;
import java.util.List;

import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.guild.GuildData;
import bot.antony.guild.UserData;

/**
 * Class to store data about requested ant species which were not available during search
 */
public class AvailabilityNotificationRequest {
	private Specie ant;
	private GuildData guild;
	private List<UserData> user = new ArrayList<UserData>();
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AvailabilityNotificationRequest() {
		super();
	}
	
	public AvailabilityNotificationRequest(Specie ant) {
		super();
		this.ant = ant;
	}
	
	public AvailabilityNotificationRequest(Specie ant, GuildData guild) {
		super();
		this.ant = ant;
		this.guild = guild;
	}
	
	public AvailabilityNotificationRequest(Specie ant, GuildData guild, UserData user) {
		super();
		this.ant = ant;
		this.guild = guild;
		addUser(user);
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public boolean addUser(UserData newUser) {
		if(!user.contains(newUser)) {
			user.add(newUser);
			return true;
		}
		return false;
	}

	public Specie getAnt() {
		return ant;
	}

	public void setAnt(Specie ant) {
		this.ant = ant;
	}

	public List<UserData> getUser() {
		return user;
	}

	public void setUser(List<UserData> user) {
		this.user = user;
	}
	
	public GuildData getGuild() {
		return guild;
	}

	public void setGuild(GuildData guild) {
		this.guild = guild;
	}

	@Override
	public boolean equals(Object o) {
		// If the object is compared with itself then return true
		if (o == this) {
			return true;
		}
		// Check if o is an instance of AvailabilityNotificationRequest or not "null instanceof [type]" also returns false
		if(!(o instanceof AvailabilityNotificationRequest)) {
			return false;
		}
		// Typecast o to AvailabilityNotificationRequest so that we can compare data
		AvailabilityNotificationRequest anr = (AvailabilityNotificationRequest) o;
		// Compare the Specie and discord guild because these are what matters for notification requests
		if(ant.equals(anr.getAnt())) {
			return true;
		}
		return false;
	}
}
