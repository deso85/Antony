package bot.antony;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import bot.antony.commands.*;
import bot.antony.commands.aam.AddHBCmd;
import bot.antony.commands.antcheck.AntcheckCmd;
import bot.antony.commands.emergency.EmergencyCmd;
import bot.antony.commands.giveaway.GiveawayCmd;
import bot.antony.commands.lists.BlacklistCmd;
import bot.antony.commands.lists.WatchlistCmd;
import bot.antony.commands.lists.WhitelistCmd;
import bot.antony.commands.reminder.ReminderCmd;
import bot.antony.commands.shopping.ShoppingCmd;
import bot.antony.commands.shopping.ShoppingManagerCmd;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class CommandManager {

    // Encapsulated, immutable maps
    private final Map<String, ServerCommand> commands;   // canonical command key -> command
    private final Map<String, String> aliases;           // alias key -> canonical command key

    public CommandManager() {
        // Keep LinkedHashMap to preserve insertion order (e.g., for help listings)
        LinkedHashMap<String, ServerCommand> cmds = new LinkedHashMap<>();
        cmds.put("antony", new AntonyCmd());
        cmds.put("help", new HelpCmd());
        cmds.put("changelog", new ChangelogCmd());
        cmds.put("addhb", new AddHBCmd());
        cmds.put("antcheck", new AntcheckCmd());
        cmds.put("archive", new ArchiveCmd());
        cmds.put("beer", new BeerCmd());
        cmds.put("blacklist", new BlacklistCmd());
        cmds.put("category", new CategoryCmd());
        cmds.put("channel", new ChannelCmd());
        cmds.put("coffee", new CoffeeCmd());
        cmds.put("command", new CommandCmd());
        cmds.put("emergency", new EmergencyCmd());
        cmds.put("giveaway", new GiveawayCmd());
        cmds.put("guild", new GuildCmd());
        cmds.put("map", new MapCmd());
        cmds.put("notify", new NotifyCmd());
        cmds.put("offernotification", new OfferNotificationCmd());
        cmds.put("offers", new OffersCmd());
        cmds.put("pnlink", new PnLinkCmd());
        cmds.put("reaction", new ReactionCmd());
        cmds.put("reminder", new ReminderCmd());
        cmds.put("sells", new SellsCmd());
        cmds.put("serverstats", new ServerstatsCmd());
        cmds.put("softban", new SoftbanCmd());
        cmds.put("shopping", new ShoppingCmd());
        cmds.put("shoppingmanager", new ShoppingManagerCmd());
        cmds.put("shops", new ShopsCmd());
        cmds.put("showavatar", new ShowAvatarCmd());
        cmds.put("shutdown", new ShutdownCmd());
        cmds.put("user", new UserCmd());
        cmds.put("userinfo", new UserInfoCmd());
        cmds.put("watchlist", new WatchlistCmd());
        cmds.put("whitelist", new WhitelistCmd());
        this.commands = Collections.unmodifiableMap(cmds);

        // Aliases map alias -> canonical key
        LinkedHashMap<String, String> als = new LinkedHashMap<>();
        als.put("hilfe", "help");
        als.put("notfall", "emergency");
        als.put("shoppinglist", "shopping");
        als.put("einkaufsliste", "shopping");
        als.put("karte", "map");
        als.put("avatar", "showavatar");
        als.put("bier", "beer");
        als.put("kaffee", "coffee");
        als.put("stats", "serverstats");
        this.aliases = Collections.unmodifiableMap(als);
    }

    public boolean perform(String rawCommand, Member member, GuildMessageChannel channel, Message message) {
        if (rawCommand == null || rawCommand.isBlank()) {
            Antony.getLogger().debug("Empty command received.");
            return false;
        }

        final String normalized = normalize(rawCommand);

        // ServerCommand with alias resolution and permission check
        String key = resolveAlias(normalized);
        ServerCommand scmd = commands.get(key);
        if (scmd != null) {
            if (scmd.mayUse(member)) {
                scmd.performCommand(member, channel, message);
                Antony.getLogger().debug("Executed server command '{}' (resolved from '{}')", key, normalized);
                return true;
            } else {
                Antony.getLogger().info("No permission for server command '{}' by member '{}'", key, member != null ? member.getId() : "null");
                return false;
            }
        }

        // Unknown command
        Antony.getLogger().debug("Unknown command '{}'", normalized);
        return false;
    }

    public Map<String, ServerCommand> getAvailableCommands(Member member) {
        LinkedHashMap<String, ServerCommand> filtered = new LinkedHashMap<>();
        for (Entry<String, ServerCommand> e : commands.entrySet()) {
            if (e.getValue().mayUse(member)) {
                filtered.put(e.getKey(), e.getValue());
            }
        }
        return Collections.unmodifiableMap(filtered);
    }

    public Map<String, ServerCommand> getCommands() {
        return commands;
    }

    public boolean hasCommand(String name) {
        if (name == null) return false;
        String key = resolveAlias(normalize(name));
        return commands.containsKey(key);
    }

    public ServerCommand getCommand(String name) {
        if (name == null) return null;
        String key = resolveAlias(normalize(name));
        return commands.get(key);
    }

    // Helpers

    /** Normalize input for case-insensitive lookups. */
    private static String normalize(String s) {
        return s.trim().toLowerCase(Locale.ROOT);
    }

    /** Resolve an alias to its canonical command key. If no alias exists, return the original key. */
    private String resolveAlias(String key) {
        return aliases.getOrDefault(key, key);
    }
}
