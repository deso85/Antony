package bot.antony;

import java.util.concurrent.ConcurrentHashMap;

import bot.antony.commands.AntonyHelp;
import bot.antony.commands.Sells;
import bot.antony.commands.Channel;
import bot.antony.commands.Notify;
import bot.antony.commands.Emergency;
import bot.antony.commands.EmoteAuswertung;
import bot.antony.commands.Giveaway;
import bot.antony.commands.Map;
import bot.antony.commands.PnLink;
import bot.antony.commands.Shopping;
import bot.antony.commands.ShowAvatar;
import bot.antony.commands.Shutdown;
import bot.antony.commands.Softban;
import bot.antony.commands.UserInfo;
import bot.antony.commands.Watchlist;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class CommandManager {

	public ConcurrentHashMap<String, ServerCommand> commands;

	public CommandManager() {
		
		this.commands = new ConcurrentHashMap<>();

		// Everyone
		this.commands.put("antony", new AntonyHelp());
		this.commands.put("emergency", new Emergency());
		this.commands.put("giveaway", new Giveaway());
		this.commands.put("map", new Map());
		this.commands.put("notify", new Notify());
		this.commands.put("pnlink", new PnLink());
		this.commands.put("sells", new Sells());
		this.commands.put("shopping", new Shopping());
		this.commands.put("showavatar", new ShowAvatar());
		this.commands.put("userinfo", new UserInfo());

		// Mod
		this.commands.put("watchlist", new Watchlist());
		this.commands.put("softban", new Softban());

		// Admin
		this.commands.put("channel", new Channel());
		this.commands.put("shutdown", new Shutdown());
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
