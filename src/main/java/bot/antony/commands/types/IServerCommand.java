package bot.antony.commands.types;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public interface IServerCommand {

	public void performCommand(Member member, TextChannel channel, Message message);
	
}
