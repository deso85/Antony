package bot.antony;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import bot.antony.events.reaction.add.AamProposalDecision;
import bot.antony.events.reaction.add.EggReaction;
import bot.antony.events.reaction.add.HammerReaction;
import bot.antony.events.reaction.add.ImageReaction;
import bot.antony.events.reaction.add.MessageReaction;
import bot.antony.events.reaction.add.MuteReaction;
import bot.antony.events.reaction.add.RedFlagReaction;
import bot.antony.events.reaction.add.SpyReaction;
import bot.antony.events.reaction.add.YellowSquareReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * ReactionManager for JDA 5.6.1
 *
 * - Normalizes Unicode emoji variants (VS16, ZWJ, skin tones, gender symbols)
 * - Supports aliases (different emojis mapped to the same canonical key)
 * - Optionally applies simple family rules (e.g., 🔇/🔈/🔉/🔊 → "speaker")
 * - Works with both Unicode and custom (server) emojis
 */
public class ReactionManager {

    // Canonical key -> handler instance
    private final Map<String, MessageReaction> reactions = new LinkedHashMap<>();
    // Alias (different emoji) -> canonical key
    private final Map<String, String> aliases = new HashMap<>();

    private static final int ZWJ  = 0x200D; // Zero Width Joiner
    private static final int VS16 = 0xFE0F; // Variation Selector-16

    public ReactionManager() {
        // === 1) Canonical registrations (one per logical action) ===
        register("🥚", new EggReaction());
        register("🕵", new SpyReaction());          // all 🕵️ variants collapse via normalize()
        register("🖼", new ImageReaction());
        register("🔨", new HammerReaction());
        register("🟨", new YellowSquareReaction());

        // Text / custom emoji (server emoji names or plain text)
        register("redflag", new RedFlagReaction());
        register("ausstehend", new AamProposalDecision());
        register("abgelehnt", new AamProposalDecision());
        register("akzeptiert", new AamProposalDecision());
        register("abgeschlossen", new AamProposalDecision());

        // === 2) Aliases ONLY where different code points should behave the same ===
        // Speaker family: 🔇/🔈/🔉/🔊 are different emojis → group under "speaker"
        register("speaker", new MuteReaction());
        alias("speaker", "🔇", "🔈", "🔉", "🔊");

        // No alias needed for 🕵 or 🖼 — normalize() already collapses their variants.
    }

    /** Executes the matching handler if one exists. */
    public boolean perform(MessageReactionAddEvent event) {
        String raw = getEmojiName(event);   // raw name ("🕵️‍♂️" or "redflag")
        String key = resolveKey(raw);       // canonical key after normalize/alias

        MessageReaction handler = reactions.get(key);
        if (handler != null) {
            // Pass canonical key to the handler so permission checks use the same key
            handler.setCanonicalKey(key);
            handler.perform(event);
            return true;
        }
        return false;
    }

    // ---- Public helpers (kept for compatibility with your existing code) ----

    public Map<String, MessageReaction> getReactions() {
        return reactions;
    }

    public boolean hasReaction(String name) {
        return reactions.containsKey(resolveKey(name));
    }

    public MessageReaction getReaction(String name) {
        return reactions.get(resolveKey(name));
    }

    // ---- Internals ----

    private void register(String canonicalKey, MessageReaction handler) {
        reactions.put(normalize(canonicalKey), handler);
    }

    private void alias(String canonicalKey, String... variants) {
        String normCanon = normalize(canonicalKey);
        for (String v : variants) {
            aliases.put(normalize(v), normCanon);
        }
    }

    /**
     * Returns a string representation of the emoji:
     * - Unicode → actual emoji symbol (e.g., "🕵️‍♂️")
     * - Custom  → emoji name (e.g., "redflag")
     */
    private String getEmojiName(MessageReactionAddEvent event) {
        EmojiUnion emoji = event.getEmoji();
        if (emoji.getType() == Emoji.Type.CUSTOM) {
            return emoji.asCustom().getName();
        } else {
            return emoji.getName();
        }
    }

    /**
     * Resolution pipeline:
     * 1) Normalize input (remove variants)
     * 2) Direct match?
     * 3) Alias lookup?
     * 4) Simple family rule for speaker (optional convenience)
     * 5) Fallback: normalized form
     */
    private String resolveKey(String raw) {
        String norm = normalize(raw);

        if (reactions.containsKey(norm)) return norm;

        String viaAlias = aliases.get(norm);
        if (viaAlias != null) return viaAlias;

        // Speaker family: 🔇 U+1F507 .. 🔊 U+1F50A (optional convenience)
        int first = firstCodePoint(norm);
        if (first >= 0x1F507 && first <= 0x1F50A) return "speaker";

        return norm;
    }

    /**
     * Normalization:
     * - Cut at first ZWJ (e.g., 🕵️‍♂️ → 🕵️)
     * - Remove VS16, Fitzpatrick modifiers, gender symbols, format chars
     * - Remove whitespace, lower-case (for text/custom emoji names)
     */
    private String normalize(String s) {
        if (s == null || s.isEmpty()) return "";

        // Cut at first ZWJ (Zero Width Joiner)
        int idxZWJ = s.indexOf(ZWJ);
        if (idxZWJ >= 0) {
            int cpCount = s.codePointCount(0, idxZWJ);
            int endIdx = s.offsetByCodePoints(0, cpCount);
            s = s.substring(0, endIdx);
        }

        // Filter codepoints
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);

            if (cp == VS16) continue;                    // Variation Selector-16
            if (isFitzpatrick(cp)) continue;             // Skin tone modifiers
            if (isGenderSign(cp)) continue;              // ♀ / ♂
            if (Character.getType(cp) == Character.FORMAT) continue; // Other format chars

            sb.appendCodePoint(cp);
        }

        return sb.toString().replaceAll("\\s+", "").trim().toLowerCase();
    }

    private static boolean isFitzpatrick(int cp) {
        return cp >= 0x1F3FB && cp <= 0x1F3FF;
    }

    private static boolean isGenderSign(int cp) {
        return cp == 0x2640 || cp == 0x2642; // ♀ / ♂
    }

    private static int firstCodePoint(String s) {
        if (s == null || s.isEmpty()) return -1;
        return s.codePointAt(0);
    }
}
