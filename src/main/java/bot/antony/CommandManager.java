package bot.antony;

import java.util.concurrent.ConcurrentHashMap;

import bot.antony.commands.AntonyHelp;
import bot.antony.commands.CallAntcheck;
import bot.antony.commands.ChannelUpdateNotification;
import bot.antony.commands.PerformGiveaway;
import bot.antony.commands.PostPnLink;
import bot.antony.commands.ShowAvatar;
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

		// Administrative
		this.commands.put("shutdown", new ShutdownBot());
		
		// Ant Specific
		this.commands.put("sells", new CallAntcheck());

		// Miscellaneous
		this.commands.put("antony", new AntonyHelp());
		this.commands.put("giveaway", new PerformGiveaway());
		this.commands.put("notify", new ChannelUpdateNotification());
		this.commands.put("showavatar", new ShowAvatar());
		this.commands.put("userinfo", new UserInfo());
		this.commands.put("pnlink", new PostPnLink());
		
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
