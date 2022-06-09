package bot.antony.commands;

import bot.antony.commands.types.IServerCommand;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Shutdown implements IServerCommand {

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		channel.sendMessage("Shutting down ...").queue();
		member.getJDA().getPresence().setStatus(OnlineStatus.OFFLINE);
		member.getJDA().shutdown();
		
	}

}
