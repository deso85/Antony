package bot.antony;

import java.util.concurrent.ConcurrentHashMap;

import bot.antony.commands.AntonyHelp;
import bot.antony.commands.Archive;
import bot.antony.commands.Category;
import bot.antony.commands.Channel;
import bot.antony.commands.Giveaway;
import bot.antony.commands.Guild;
import bot.antony.commands.Map;
import bot.antony.commands.Notify;
import bot.antony.commands.PnLink;
import bot.antony.commands.Sells;
import bot.antony.commands.Serverstats;
import bot.antony.commands.Shopping;
import bot.antony.commands.ShowAvatar;
import bot.antony.commands.Shutdown;
import bot.antony.commands.Softban;
import bot.antony.commands.User;
import bot.antony.commands.UserInfo;
import bot.antony.commands.aam.AddHB;
import bot.antony.commands.emergency.Emergency;
import bot.antony.commands.lists.Blacklist;
import bot.antony.commands.lists.Watchlist;
import bot.antony.commands.lists.Whitelist;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class CommandManager {

	public ConcurrentHashMap<String, ServerCommand> usrCommands;
	public ConcurrentHashMap<String, ServerCommand> modCommands;
	public ConcurrentHashMap<String, ServerCommand> adminCommands;

	public CommandManager() {
		
		this.usrCommands = new ConcurrentHashMap<>();
		this.modCommands = new ConcurrentHashMap<>();
		this.adminCommands = new ConcurrentHashMap<>();

		// Everyone
		this.usrCommands.put("antony", new AntonyHelp());
		this.usrCommands.put("addhb", new AddHB());
		this.usrCommands.put("emergency", new Emergency());
		this.usrCommands.put("giveaway", new Giveaway());
		this.usrCommands.put("map", new Map());
		this.usrCommands.put("notify", new Notify());
		this.usrCommands.put("pnlink", new PnLink());
		this.usrCommands.put("sells", new Sells());
		this.usrCommands.put("serverstats", new Serverstats());
		this.usrCommands.put("shopping", new Shopping());
		this.usrCommands.put("showavatar", new ShowAvatar());
		this.usrCommands.put("userinfo", new UserInfo());

		// Mod
		this.modCommands.put("user", new User());
		this.modCommands.put("softban", new Softban());
		this.modCommands.put("watchlist", new Watchlist());
		this.modCommands.put("whitelist", new Whitelist());

		// Admin
		this.adminCommands.put("archive", new Archive());
		this.adminCommands.put("blacklist", new Blacklist());
		this.adminCommands.put("category", new Category());
		this.adminCommands.put("channel", new Channel());
		this.adminCommands.put("guild", new Guild());
		this.adminCommands.put("shutdown", new Shutdown());
	}

	public boolean perform(String command, Member member, TextChannel channel, Message message) {

		ServerCommand cmd;
		//Commands for everyone
		if ((cmd = this.usrCommands.get(command.toLowerCase())) != null) {
			cmd.performCommand(member, channel, message);
			return true;
		}
		//Commands for mods
		if ((cmd = this.modCommands.get(command.toLowerCase())) != null) {
			if(Antony.getGuildController().memberIsMod(member)) {
				cmd.performCommand(member, channel, message);
				return true;
			}
		}
		//Commands for admins
		if ((cmd = this.adminCommands.get(command.toLowerCase())) != null) {
			if(Antony.getGuildController().memberIsAdmin(member)) {
				cmd.performCommand(member, channel, message);
				return true;
			}
		}

		return false;
	}
}
