package bot.antony.commands;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.events.softban.UserDataSB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class SoftbanCmd extends ServerCommand {

    private static final int DISCORD_MESSAGE_LIMIT = 2000;
    private static final DateTimeFormatter LOG_TS =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.GERMANY);

    public SoftbanCmd() {
        super();
        this.privileged = true;
        this.name = "softban";
        this.shortDescription = "Verwaltet Softbans (add/remove/list/reload/clear).";
        this.description = "Fügt Softbans hinzu/entfernt sie, listet sie auf oder lädt/cleart die Liste.";
        this.example = "add @SomeUser\nadd 123456789012345678 Some User";
        this.cmdParams.put("add <ID|@Mention> [NAME]", "Setzt einen Softban. NAME ist optional; wird sonst automatisch ermittelt.");
        this.cmdParams.put("remove <ID|@Mention> <NAME>", "Entfernt den Softban.");
        this.cmdParams.put("list", "Listet alle aktuell softgebannten User auf.");
        this.cmdParams.put("reload", "Lädt die Softban-Liste neu (von persistierten Daten).");
        this.cmdParams.put("clear", "Leert die Softban-Liste und persistiert den leeren Zustand.");
    }

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {
        Guild guild = channel.getGuild();
        TextChannel logChannel = Antony.getGuildController().getLogChannel(guild);

        // Raw text so mention tokens bleiben erhalten (<@123>)
        String[] parts = message.getContentRaw().trim().split("\\s+", 4);
        if (parts.length <= 1) { super.printHelp(channel); return; }

        String action = parts[1].toLowerCase(Locale.ROOT);

        switch (action) {
            case "add": {
                if (parts.length < 3) { super.printHelp(channel); break; }

                // FINAL: ID nur einmal bestimmen (sonst nicht "effektiv final" für Lambda)
                final String id = coalesceUserId(message, parts[2]);
                if (id == null) { super.printHelp(channel); break; }

                // Name optional – wenn fehlt, asynchron auflösen
                if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
                    final String name = parts[3].trim();
                    proceedAdd(member, guild, channel, message, logChannel, id, name);
                } else {
                    resolveName(guild, id, resolvedName -> proceedAdd(member, guild, channel, message, logChannel, id, resolvedName));
                }
                break;
            }

            case "remove": {
                if (parts.length < 3) { super.printHelp(channel); break; }

                final String id = coalesceUserId(message, parts[2]);
                if (id == null || parts.length < 4) { super.printHelp(channel); break; }

                final String name = parts[3].trim();
                UserDataSB user = new UserDataSB(id, name);
                if (Antony.getSoftbanController().unban(user)) {
                    if (logChannel != null) {
                        logChannel.sendMessage("🟢 User \"" + name + "\" softban removed by " + member.getUser().getAsMention()).queue();
                        logChannel.sendMessageEmbeds(buildLogEmbed(Color.GREEN, "Softban entfernt", id, name, channel, message)).queue();
                    }
                    channel.sendMessage("User \"" + name + "\" wurde entbannt.").queue();

                    // Hinweis, falls kein Guild-Mitglied
                    guild.retrieveMemberById(id).queue(
                            m -> { /* ok */ },
                            err -> channel.sendMessage("Hinweis: Die ID `" + id + "` ist kein Mitglied dieses Servers.").queue()
                    );
                } else {
                    channel.sendMessage("User \"" + name + "\" konnte nicht entbannt werden.").queue();
                }
                break;
            }

            case "list": {
                List<UserDataSB> banned = Antony.getSoftbanController().getBannedUser();
                if (banned == null || banned.isEmpty()) {
                    channel.sendMessage("Es sind keine User softbanned.").queue();
                } else {
                    String header = "Folgende User sind softbanned:\n";
                    ArrayList<String> lines = new ArrayList<>(banned.size());
                    for (UserDataSB u : banned) lines.add("- " + u.getName() + " (" + u.getId() + ")");
                    for (String page : paginateWithHeader(header, lines, DISCORD_MESSAGE_LIMIT)) channel.sendMessage(page).queue();
                }
                break;
            }

            case "reload": {
                Antony.getSoftbanController().initData();
                int size = Antony.getSoftbanController().getBannedUser().size();
                channel.sendMessage("Die Liste wurde mit " + size + " Einträgen neu geladen.").queue();
                break;
            }

            case "clear": {
                Antony.getSoftbanController().setBannedUser(new ArrayList<>());
                Antony.getSoftbanController().persistData();
                channel.sendMessage("Die Softban-Liste wurde geleert.").queue();
                break;
            }

            default:
                super.printHelp(channel);
                break;
        }

        Antony.getLogger().debug("Executed softban by member '{}'", member != null ? member.getId() : "null");
    }

    // ---------- flow helpers ----------

    private void proceedAdd(Member moderator, Guild guild, GuildMessageChannel channel, Message message,
                            TextChannel logChannel, String id, String name) {
        UserDataSB user = new UserDataSB(id, name);
        if (Antony.getSoftbanController().ban(user)) {
            if (logChannel != null) {
                logChannel.sendMessage("🔨 User \"" + name + "\" manually soft banned by " + moderator.getUser().getAsMention()).queue();
                logChannel.sendMessageEmbeds(buildLogEmbed(Color.RED, "Softban hinzugefügt", id, name, channel, message)).queue();
            }
            channel.sendMessage("User \"" + name + "\" wurde softbanned.").queue();

            guild.retrieveMemberById(id).queue(
                    m -> { /* ok */ },
                    err -> channel.sendMessage("Hinweis: Die ID `" + id + "` ist kein Mitglied dieses Servers (Softban wurde dennoch gesetzt).").queue()
            );
        } else {
            channel.sendMessage("User \"" + name + "\" konnte nicht gebannt werden.").queue();
        }
    }

    private void resolveName(Guild guild, String id, Consumer<String> onResolved) {
        guild.retrieveMemberById(id).queue(
                m -> onResolved.accept(m.getEffectiveName()),
                err -> guild.getJDA().retrieveUserById(id).queue(
                        (User u) -> onResolved.accept(u.getGlobalName() != null && !u.getGlobalName().isBlank() ? u.getGlobalName() : u.getName()),
                        err2 -> onResolved.accept(id) // last resort
                )
        );
    }

    // ---------- utility helpers ----------

    /** Liefert die erste erwähnte User-ID, sonst parsed sie den Token; beides ohne Re-Zuweisung → "effektiv final". */
    private static String coalesceUserId(Message message, String token) {
        String mentioned = firstMentionedUserId(message);
        return mentioned != null ? mentioned : extractUserId(token);
    }

    /** Prefer the first actually mentioned user; returns null if none. */
    private static String firstMentionedUserId(Message message) {
        List<User> mentioned = message.getMentions().getUsers();
        return mentioned.isEmpty() ? null : mentioned.get(0).getId();
    }

    /** Extracts a user ID from a token (supports <@123>, <@!123>, or plain digits). Returns null if invalid. */
    private static String extractUserId(String token) {
        if (token == null || token.isBlank()) return null;
        token = token.trim();
        if (token.startsWith("<@") && token.endsWith(">")) {
            String inner = token.substring(2, token.length() - 1);
            if (inner.startsWith("!")) inner = inner.substring(1);
            return inner.matches("\\d{5,20}") ? inner : null;
        }
        return token.matches("\\d{5,20}") ? token : null;
    }

    private static String buildMessageLink(GuildMessageChannel channel, Message message) {
        return "https://discord.com/channels/"
                + channel.getGuild().getId() + "/"
                + channel.getId() + "/"
                + message.getId();
    }

    private static MessageEmbed buildLogEmbed(Color color, String title, String id, String name,
                                              GuildMessageChannel channel, Message message) {
        return new EmbedBuilder()
                .setColor(color)
                .setAuthor(name + " | ID: " + id)
                .setTitle(title)
                .setDescription(message.getContentDisplay())
                .addField("#" + channel.getName(),
                        "**[Hier klicken, um zur Nachricht zu kommen.](" + buildMessageLink(channel, message) + ")**",
                        false)
                .setFooter(LOG_TS.format(OffsetDateTime.now()))
                .build();
    }

    private static List<String> paginateWithHeader(String header, List<String> lines, int maxLen) {
        List<String> pages = new ArrayList<>();
        StringBuilder cur = new StringBuilder(header);
        for (String line : lines) {
            if (cur.length() + line.length() + 1 > maxLen) {
                pages.add(cur.toString());
                cur = new StringBuilder(); // no header on subsequent pages
            }
            if (cur.length() > 0) cur.append('\n');
            cur.append(line);
        }
        if (cur.length() > 0) pages.add(cur.toString());
        return pages;
    }
}
