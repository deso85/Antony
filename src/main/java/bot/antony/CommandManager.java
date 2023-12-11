package bot.antony;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import bot.antony.commands.AntonyCmd;
import bot.antony.commands.ArchiveCmd;
import bot.antony.commands.BeerCmd;
import bot.antony.commands.CategoryCmd;
import bot.antony.commands.ChangelogCmd;
import bot.antony.commands.ChannelCmd;
import bot.antony.commands.CoffeeCmd;
import bot.antony.commands.CommandCmd;
import bot.antony.commands.GuildCmd;
import bot.antony.commands.HelpCmd;
import bot.antony.commands.MapCmd;
import bot.antony.commands.NotifyCmd;
import bot.antony.commands.OfferNotificationCmd;
import bot.antony.commands.PnLinkCmd;
import bot.antony.commands.ReactionCmd;
import bot.antony.commands.SellsCmd;
import bot.antony.commands.Serverstats;
import bot.antony.commands.ShoppingCmd;
import bot.antony.commands.ShopsCmd;
import bot.antony.commands.ShowAvatarCmd;
import bot.antony.commands.ShutdownCmd;
import bot.antony.commands.Softban;
import bot.antony.commands.UserCmd;
import bot.antony.commands.UserInfoCmd;
import bot.antony.commands.aam.AddHBCmd;
import bot.antony.commands.antcheck.AntcheckCmd;
import bot.antony.commands.emergency.EmergencyCmd;
import bot.antony.commands.giveaway.GiveawayCmd;
import bot.antony.commands.lists.BlacklistCmd;
import bot.antony.commands.lists.WatchlistCmd;
import bot.antony.commands.lists.WhitelistCmd;
import bot.antony.commands.reminder.ReminderCmd;
import bot.antony.commands.types.IServerCommand;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class CommandManager {

	public LinkedHashMap <String, ServerCommand> commands = new LinkedHashMap <String, ServerCommand>();
	public LinkedHashMap <String, ServerCommand> aliases = new LinkedHashMap <String, ServerCommand>();
	public ConcurrentHashMap<String, IServerCommand> usrCommands;
	public ConcurrentHashMap<String, IServerCommand> modCommands;
	public ConcurrentHashMap<String, IServerCommand> adminCommands;

	public CommandManager() {
		commands.put("antcheck", new AntcheckCmd());
		commands.put("antony", new AntonyCmd());
		commands.put("help", new HelpCmd());
		commands.put("changelog", new ChangelogCmd());
		commands.put("addhb", new AddHBCmd());
		commands.put("archive", new ArchiveCmd());
		commands.put("beer", new BeerCmd());
		commands.put("blacklist", new BlacklistCmd());
		commands.put("category", new CategoryCmd());
		commands.put("channel", new ChannelCmd());
		commands.put("coffee", new CoffeeCmd());
		commands.put("command", new CommandCmd());
		commands.put("emergency", new EmergencyCmd());
		commands.put("giveaway", new GiveawayCmd());
		commands.put("guild", new GuildCmd());
		commands.put("map", new MapCmd());
		commands.put("notify", new NotifyCmd());
		commands.put("offernotification", new OfferNotificationCmd());
		commands.put("pnlink", new PnLinkCmd());
		commands.put("reaction", new ReactionCmd());
		commands.put("reminder", new ReminderCmd());
		commands.put("sells", new SellsCmd());
		commands.put("shopping", new ShoppingCmd());
		commands.put("shops", new ShopsCmd());
		commands.put("showavatar", new ShowAvatarCmd());
		commands.put("shutdown", new ShutdownCmd());
		commands.put("user", new UserCmd());
		commands.put("userinfo", new UserInfoCmd());
		commands.put("watchlist", new WatchlistCmd());
		commands.put("whitelist", new WhitelistCmd());
		
		aliases.put("hilfe", commands.get("help"));
		aliases.put("notfall", commands.get("emergency"));
		aliases.put("shoppinglist", commands.get("shopping"));
		aliases.put("einkaufsliste", commands.get("shopping"));
		aliases.put("karte", commands.get("map"));
		aliases.put("avatar", commands.get("showavatar"));
		aliases.put("bier", commands.get("beer"));
		aliases.put("kaffee", commands.get("coffee"));
		
		usrCommands = new ConcurrentHashMap<>();
		modCommands = new ConcurrentHashMap<>();

		// Everyone
		usrCommands.put("serverstats", new Serverstats());

		// Mod
		modCommands.put("softban", new Softban());
	}

	public boolean perform(String command, Member member, GuildMessageChannel channel, Message message) {

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
			if(cmd.mayUse(member)) {
				cmd.performCommand(member, channel, message);
				return true;
			}
		}
		//Aliases
		if((cmd = this.aliases.get(command.toLowerCase())) != null) {
			if(cmd.mayUse(member)) {
				cmd.performCommand(member, channel, message);
				return true;
			}
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
