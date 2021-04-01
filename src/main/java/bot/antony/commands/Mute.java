package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Mute implements ServerCommand {

	private TextChannel channel;
	
	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		setChannel(channel);
		List<String> allowedRoles = new ArrayList<String>();
		
		//Roles which may use the command
		allowedRoles.add("Admin");
		allowedRoles.add("Soldat");
		allowedRoles.add("Intermorphe");
		
		boolean mayUse = false;
		for(Role role: m.getRoles()) {
			if(allowedRoles.contains(role.getName())) {
				mayUse = true;
			}
		}
		
		if(mayUse) {
			
		}
		
	}
	
	private void printHelp() {
		//TODO: Help ausformulieren
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "mute (Nickname|ID) [Time] [Reason]\nBeispiel: " + Antony.getCmdPrefix() + "mute Antony 1d Böser Bot!").queue();
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------

	public TextChannel getChannel() {
		return channel;
	}

	public void setChannel(TextChannel channel) {
		this.channel = channel;
	}

}
