package bot.antony.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class ServerstatsCmd extends ServerCommand {

    private static final int EMBED_FIELD_LIMIT = 1024;

    public ServerstatsCmd() {
        super();
        this.privileged = false;           // public command
        this.name = "serverstats";
        this.shortDescription = "Zeigt grundlegende Server-Statistiken.";
        this.description = "Gibt Infos zu Boosts, Emojis, Kategorien, Kanälen, User-Status und Rollen aus.";
        this.example = ""; // no args
    }

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {
        Guild guild = channel.getGuild();

        // --- Collect server stats ---
        int boostCount = guild.getBoostCount();
        String boostTier = String.valueOf(guild.getBoostTier());

        int emoteCount = guild.getEmojis().size();
        int maxEmoteCount = guild.getMaxEmojis();

        int categoryCount = guild.getCategories().size();
        int textChannelCount = guild.getTextChannels().size();
        int voiceChannelCount = guild.getVoiceChannels().size();
        int channelCount = guild.getChannels().size();

        // Presence breakdown only if the presence intent is enabled
        boolean hasPresenceIntent = member != null
                && member.getJDA().getGatewayIntents().contains(GatewayIntent.GUILD_PRESENCES);

        int offlineMemberCount = 0;
        int dndMemberCount = 0;
        int idleMemberCount = 0;
        int onlineMemberCount = 0;
        int botCount = 0;
        int realMemberCount = 0;

        // --- Role counting in one pass over members (faster than getMembersWithRoles(role) per role) ---
        // Keep guild role order (and skip @everyone)
        Map<Role, Integer> roleCounts = new LinkedHashMap<>();
        for (Role role : guild.getRoles()) {
            if (role.isPublicRole()) continue; // skip @everyone
            roleCounts.put(role, 0);
        }

        for (Member mbr : guild.getMembers()) {
            if (mbr.getUser().isBot()) botCount++; else realMemberCount++;

            if (hasPresenceIntent) {
                OnlineStatus st = mbr.getOnlineStatus();
                if (st == OnlineStatus.ONLINE) onlineMemberCount++;
                else if (st == OnlineStatus.DO_NOT_DISTURB) dndMemberCount++;
                else if (st == OnlineStatus.IDLE) idleMemberCount++;
                else offlineMemberCount++; // OFFLINE/INVISIBLE/UNKNOWN
            }

            // increment role counters for this member
            for (Role r : mbr.getRoles()) {
                Integer cnt = roleCounts.get(r);
                if (cnt != null) roleCounts.put(r, cnt + 1);
            }
        }

        int userCount = guild.getMemberCount();
        int roleCount = roleCounts.size();

        // Build role lines (guild order)
        List<String> roleLines = new ArrayList<>(roleCount);
        for (Map.Entry<Role, Integer> e : roleCounts.entrySet()) {
            roleLines.add(e.getKey().getAsMention() + ": " + e.getValue());
        }

        // --- Build embed ---
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("***" + guild.getName() + "***")
                .setColor(Antony.getBaseColor())
                .setDescription("Im folgenden werden Server-Statistiken ausgegeben.")
                .setThumbnail(guild.getIconUrl())
                .setFooter("Antony Version " + Antony.getVersion());

        // Basics
        eb.addField("Allgemeines",
                "Server erstellt: <t:" + guild.getTimeCreated().toEpochSecond() + ":D>"
                        + "\nServer-Boosts: " + boostCount
                        + "\nBoost Tier: " + boostTier
                        + "\nEmote Count: " + emoteCount + " / " + maxEmoteCount,
                false);

        // Categories and Channels
        eb.addField("Anzahl Kategorien und Kanäle",
                "Kategorien: " + categoryCount
                        + "\nText-Kanäle: " + textChannelCount
                        + "\nVoice-Kanäle: " + voiceChannelCount
                        + "\nGesamt: " + channelCount + " / 500",
                false);

        // Users (presence block shown only if intent is enabled)
        StringBuilder users = new StringBuilder()
                .append("User: ").append(realMemberCount)
                .append("\nBots: ").append(botCount)
                .append("\nGesamt: ").append(userCount);

        if (hasPresenceIntent) {
            users.append("\n\nUser Online: ").append(onlineMemberCount)
                    .append("\nUser DND: ").append(dndMemberCount)
                    .append("\nUser Idle: ").append(idleMemberCount)
                    .append("\nUser Offline: ").append(offlineMemberCount);
        } else {
            users.append("\n\nStatusverteilung nicht verfügbar (Presence-Intent deaktiviert).");
        }
        eb.addField("User", users.toString(), false);

        // Roles (chunked to stay within 1024 chars per field)
        if (roleLines.isEmpty()) {
            eb.addField("Rollen", "Keine Rollen gefunden.", false);
        } else {
            final String rolesHeader = "Rollen: " + roleCount + "\n";
            List<String> chunks = chunkLinesWithFirstPrefix(roleLines, EMBED_FIELD_LIMIT, rolesHeader.length());

            if (chunks.size() == 1) {
                eb.addField("Rollen", rolesHeader + chunks.get(0), false);
            } else {
                // First page with header
                eb.addField("Rollen (1/" + chunks.size() + ")", rolesHeader + chunks.get(0), false);
                // Continuation pages without header
                for (int i = 1; i < chunks.size(); i++) {
                    eb.addField("Rollen (" + (i + 1) + "/" + chunks.size() + ")", chunks.get(i), false);
                }
            }
        }

        channel.sendMessageEmbeds(eb.build()).queue();
        Antony.getLogger().debug("Executed serverstats by member '{}'", member != null ? member.getId() : "null");
    }

    // -------------- helpers --------------

    /**
     * Chunks lines so each chunk stays within 'limit' characters.
     * The first chunk reserves 'firstPrefixLen' characters for a header that will be prepended externally.
     */
    private static List<String> chunkLinesWithFirstPrefix(List<String> lines, int limit, int firstPrefixLen) {
        List<String> chunks = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean firstChunk = true;
        int currentLimit = limit - firstPrefixLen;

        for (String line : lines) {
            String candidate = (cur.length() == 0 ? line : "\n" + line);
            if (cur.length() + candidate.length() > currentLimit) {
                // flush current chunk
                if (cur.length() > 0) chunks.add(cur.toString());
                cur = new StringBuilder(line);
                // after first flush, no special prefix on subsequent chunks
                if (firstChunk) {
                    firstChunk = false;
                    currentLimit = limit;
                }
            } else {
                cur.append(candidate);
            }
        }
        if (cur.length() > 0) chunks.add(cur.toString());
        return chunks;
    }
}
