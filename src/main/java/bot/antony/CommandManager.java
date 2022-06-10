package bot.antony;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import bot.antony.commands.AntonyHelp;
import bot.antony.commands.Archive;
import bot.antony.commands.Category;
import bot.antony.commands.Changelog;
import bot.antony.commands.Channel;
import bot.antony.commands.Command;
import bot.antony.commands.Giveaway;
import bot.antony.commands.Guild;
import bot.antony.commands.Help;
import bot.antony.commands.Map;
import bot.antony.commands.Notify;
import bot.antony.commands.PnLink;
import bot.antony.commands.Reaction;
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
import bot.antony.commands.types.IServerCommand;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class CommandManager {

	public LinkedHashMap <String, ServerCommand> commands = new LinkedHashMap <String, ServerCommand>();
	public ConcurrentHashMap<String, IServerCommand> usrCommands;
	public ConcurrentHashMap<String, IServerCommand> modCommands;
	public ConcurrentHashMap<String, IServerCommand> adminCommands;

	public CommandManager() {
		commands.put("antony", new AntonyHelp());
		commands.put("help", new Help());
		commands.put("changelog", new Changelog());
		commands.put("shopping", new Shopping());
		commands.put("command", new Command());
		commands.put("reaction", new Reaction());
		commands.put("shutdown", new Shutdown());
		
		usrCommands = new ConcurrentHashMap<>();
		modCommands = new ConcurrentHashMap<>();
		adminCommands = new ConcurrentHashMap<>();

		// Everyone
		usrCommands.put("addhb", new AddHB());
		usrCommands.put("emergency", new Emergency());
		usrCommands.put("giveaway", new Giveaway());
		usrCommands.put("map", new Map());
		usrCommands.put("notify", new Notify());
		usrCommands.put("pnlink", new PnLink());
		usrCommands.put("sells", new Sells());
		usrCommands.put("serverstats", new Serverstats());
		usrCommands.put("showavatar", new ShowAvatar());
		usrCommands.put("userinfo", new UserInfo());

		// Mod
		modCommands.put("user", new User());
		modCommands.put("softban", new Softban());
		modCommands.put("watchlist", new Watchlist());
		modCommands.put("whitelist", new Whitelist());

		// Admin
		adminCommands.put("archive", new Archive());
		adminCommands.put("blacklist", new Blacklist());
		adminCommands.put("category", new Category());
		adminCommands.put("channel", new Channel());
		adminCommands.put("guild", new Guild());
	}

	public boolean perform(String command, Member member, TextChannel channel, Message message) {

		IServerCommand icmd;
		//Commands for everyone
		if ((icmd = this.usrCommands.get(command.toLowerCase())) != null) {
			icmd.performCommand(member, channel, message);
			return true;
		}
		//Commands for mods
		if ((icmd = this.modCommands.get(command.toLowerCase())) != null) {
			if(Antony.getGuildController().memberIsMod(member)) {
				icmd.performCommand(member, channel, message);
				return true;
			}
		}
		//Commands for admins
		if ((icmd = this.adminCommands.get(command.toLowerCase())) != null) {
			if(Antony.getGuildController().memberIsAdmin(member)) {
				icmd.performCommand(member, channel, message);
				return true;
			}
		}
		
		//New Commands
		ServerCommand cmd;
		if ((cmd = this.commands.get(command.toLowerCase())) != null) {
			cmd.performCommand(member, channel, message);
			return true;
		}
		return false;
	}
	
	//public LinkedList<String> listCommands() {
		/*ArrayList<String> list = new ArrayList<String>();
		commands.forEach((name, command) -> list.add(name));
		return list;*/
		//return cmdNames;
	//}
	
	public LinkedHashMap<String, ServerCommand> getAvailableCommands(Member member){
		LinkedHashMap<String, ServerCommand> filteredCmds = new LinkedHashMap <String, ServerCommand>();
		for(Entry<String, ServerCommand> entry : getCommands().entrySet()) {
			if(entry.getValue().mayUse(member)) {
				filteredCmds.put(entry.getKey(), entry.getValue());
			}
		}
		return filteredCmds;
	}
	
	public LinkedHashMap<String, ServerCommand> getCommands(){
		return commands;
	}
	
	public boolean hasCommand(String name) {
		return commands.containsKey(name);
	}
	
	public ServerCommand getCommand(String name) {
		if(hasCommand(name)) {
			return commands.get(name);
		}
		return null;
	}
}
