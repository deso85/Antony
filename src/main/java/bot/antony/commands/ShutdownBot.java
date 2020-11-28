package bot.antony.commands;

import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class ShutdownBot implements ServerCommand {

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		
		if(m.hasPermission(channel, Permission.KICK_MEMBERS)) {
			
			channel.sendMessage("Shutting down ...").queue();
			m.getJDA().getPresence().setStatus(OnlineStatus.OFFLINE);
			m.getJDA().shutdown();
			
		}
		
	}

}
