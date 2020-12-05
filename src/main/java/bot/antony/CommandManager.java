package bot.antony;

import java.util.concurrent.ConcurrentHashMap;

import bot.antony.commands.AntonyHelp;
import bot.antony.commands.CallAntcheck;
import bot.antony.commands.ShutdownBot;
import bot.antony.commands.UserInfo;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class CommandManager {

	public ConcurrentHashMap<String, ServerCommand> commands;

	public CommandManager() {
		
		this.commands = new ConcurrentHashMap<>();

		// Ant Specific
		this.commands.put("sells", new CallAntcheck());

		// Administrative
		this.commands.put("shutdown", new ShutdownBot());
		
		// Miscellaneous
		this.commands.put("antony", new AntonyHelp());
		this.commands.put("userinfo", new UserInfo());
		
	}

	public boolean perform(String command, Member m, TextChannel channel, Message message) {

		ServerCommand cmd;
		if ((cmd = this.commands.get(command.toLowerCase())) != null) {
			cmd.performCommand(m, channel, message);
			return true;
		}

		return false;
	}
}
