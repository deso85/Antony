package bot.antony;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import bot.antony.commands.AntonyCmd;
import bot.antony.commands.ArchiveCmd;
import bot.antony.commands.CategoryCmd;
import bot.antony.commands.ChangelogCmd;
import bot.antony.commands.ChannelCmd;
import bot.antony.commands.CommandCmd;
import bot.antony.commands.Giveaway;
import bot.antony.commands.GuildCmd;
import bot.antony.commands.HelpCmd;
import bot.antony.commands.MapCmd;
import bot.antony.commands.Notify;
import bot.antony.commands.PnLinkCmd;
import bot.antony.commands.ReactionCmd;
import bot.antony.commands.Sells;
import bot.antony.commands.Serverstats;
import bot.antony.commands.ShoppingCmd;
import bot.antony.commands.ShowAvatarCmd;
import bot.antony.commands.ShutdownCmd;
import bot.antony.commands.Softban;
import bot.antony.commands.UserCmd;
import bot.antony.commands.UserInfo;
import bot.antony.commands.aam.AddHBCmd;
import bot.antony.commands.emergency.EmergencyCmd;
import bot.antony.commands.lists.BlacklistCmd;
import bot.antony.commands.lists.WatchlistCmd;
import bot.antony.commands.lists.WhitelistCmd;
import bot.antony.commands.types.IServerCommand;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class CommandManager {

	public LinkedHashMap <String, ServerCommand> commands = new LinkedHashMap <String, ServerCommand>();
	public LinkedHashMap <String, ServerCommand> aliases = new LinkedHashMap <String, ServerCommand>();
	public ConcurrentHashMap<String, IServerCommand> usrCommands;
	public ConcurrentHashMap<String, IServerCommand> modCommands;
	public ConcurrentHashMap<String, IServerCommand> adminCommands;

	public CommandManager() {
		commands.put("antony", new AntonyCmd());
		commands.put("help", new HelpCmd());
		commands.put("changelog", new ChangelogCmd());
		commands.put("addhb", new AddHBCmd());
		commands.put("archive", new ArchiveCmd());
		commands.put("blacklist", new BlacklistCmd());
		commands.put("category", new CategoryCmd());
		commands.put("channel", new ChannelCmd());
		commands.put("command", new CommandCmd());
		commands.put("emergency", new EmergencyCmd());
		commands.put("guild", new GuildCmd());
		commands.put("map", new MapCmd());
		commands.put("pnlink", new PnLinkCmd());
		commands.put("reaction", new ReactionCmd());
		commands.put("shopping", new ShoppingCmd());
		commands.put("showavatar", new ShowAvatarCmd());
		commands.put("shutdown", new ShutdownCmd());
		commands.put("user", new UserCmd());
		commands.put("watchlist", new WatchlistCmd());
		commands.put("whitelist", new WhitelistCmd());
		
		aliases.put("hilfe", commands.get("help"));
		aliases.put("notfall", commands.get("emergency"));
		aliases.put("shoppinglist", commands.get("shopping"));
		aliases.put("einkaufsliste", commands.get("shopping"));
		aliases.put("karte", commands.get("map"));
		aliases.put("avatar", commands.get("showavatar"));
		
		usrCommands = new ConcurrentHashMap<>();
		modCommands = new ConcurrentHashMap<>();

		// Everyone
		usrCommands.put("giveaway", new Giveaway());
		usrCommands.put("notify", new Notify());
		usrCommands.put("sells", new Sells());
		usrCommands.put("serverstats", new Serverstats());
		usrCommands.put("userinfo", new UserInfo());

		// Mod
		modCommands.put("softban", new Softban());
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
		
		//Commands
		ServerCommand cmd;
		if ((cmd = this.commands.get(command.toLowerCase())) != null) {
			cmd.performCommand(member, channel, message);
			return true;
		}
		//Aliases
		if((cmd = this.aliases.get(command.toLowerCase())) != null) {
			cmd.performCommand(member, channel, message);
			return true;
		}
		return false;
	}
	
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
