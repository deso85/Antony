package bot.antony.commands.types;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public interface IServerCommand {

	public void performCommand(Member member, GuildMessageChannel channel, Message message);
	
}
