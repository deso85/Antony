package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;

import bot.antony.commands.types.ServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Shutdown implements ServerCommand {

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		List<String> allowedRoles = new ArrayList<String>();
		
		//Roles which may use the command
		allowedRoles.add("Admin");
		allowedRoles.add("Soldat");
		
		
		if(Utils.memberHasRole(member, allowedRoles)) {
			channel.sendMessage("Shutting down ...").queue();
			member.getJDA().getPresence().setStatus(OnlineStatus.OFFLINE);
			member.getJDA().shutdown();
		}
		
	}

}
