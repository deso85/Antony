package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;

import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Shutdown implements ServerCommand {

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		
		List<String> allowedRoles = new ArrayList<String>();
		
		//Roles which may use the command
		allowedRoles.add("Admin");
		allowedRoles.add("Soldat");
		
		boolean mayUse = false;
		for(Role role: m.getRoles()) {
			if(allowedRoles.contains(role.getName())) {
				mayUse = true;
			}
		}
		
		if(mayUse) {
			channel.sendMessage("Shutting down ...").queue();
			m.getJDA().getPresence().setStatus(OnlineStatus.OFFLINE);
			m.getJDA().shutdown();
		}
		
	}

}
