package bot.antony.events.reaction.add;

import bot.antony.Antony;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * Base class for reaction handlers.
 *
 * Notes:
 * - 'name' holds the raw emoji name from the event (useful for logging).
 * - 'canonicalKey' can be set by the ReactionManager (after normalize/alias)
 *   and is used for permission checks to keep them consistent across variants.
 */
public class MessageReaction {

    // Whether this reaction requires elevated permissions (admin/whitelist)
    protected Boolean privileged = true;

    // Raw reaction "name" from the event (e.g., "🕵️‍♂️" or a custom emoji name like "redflag")
    protected String name;

    protected String description;
    protected String shortDescription;

    // Canonical key resolved by ReactionManager (e.g., "🕵", "speaker", "redflag")
    protected String canonicalKey;

    // Event-related context
    protected Emoji emote;
    protected Guild guild;
    protected Message message;
    protected GuildMessageChannel responseChannel; // Channel to respond to reaction
    protected Member reactor;

    // Accumulated log message (optional)
    protected StringBuilder logMessage = new StringBuilder();

    // --------------------------------------------------
    // Constructors
    // --------------------------------------------------
    public MessageReaction() {
        super();
    }

    public MessageReaction(MessageReactionAddEvent event) {
        super();
        setVariables(event);
    }

    // --------------------------------------------------
    // Core flow
    // --------------------------------------------------

    /**
     * Entry point called by ReactionManager.
     * Subclasses typically override this to implement behavior.
     */
    public void perform(MessageReactionAddEvent event) {
        setVariables(event);
    }

    /**
     * Determines whether the given member is allowed to trigger this reaction.
     * Uses the canonical key (if available) so that permission checks are
     * stable across Unicode variants and alias mappings.
     */
    public boolean shallTrigger(Member member) {
        if (member == null || member.getUser().isBot()) {
            return false;
        }
        // If not privileged, allow all non-bot members
        if (!isPrivileged()) {
            return true;
        }
        // Guild owner or administrators always allowed
        if (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }
        // Use canonical key (fallback to raw 'name' if not set)
        return Antony.getGuildController().memberTriggersReactionCmd(member, getEffectivePermissionKey());
    }

    /**
     * Removes the user's reaction from the message (helpful for "ack" flows).
     */
    public void removeReaction() {
        message.removeReaction(emote, reactor.getUser()).queue();
    }

    /**
     * Mentions the reacting user in the response channel (lightweight feedback).
     */
    public void mentionReactor() {
        if (responseChannel != null) {
            responseChannel.sendMessage(emote.getFormatted() + " " + reactor.getUser().getAsMention()).queue();
        }
    }

    /**
     * Prints attachment URLs of the reacted message to the response channel.
     */
    public void printAttachments() {
        if (message.getAttachments().size() > 0) {
            responseChannel.sendMessage("Folgende Attachments wurden gepostet:").complete();
            for (Attachment attachment : message.getAttachments()) {
                responseChannel.sendMessage(attachment.getUrl()).complete();
            }
        }
    }

    /**
     * Writes accumulated log output (if any).
     */
    public void log() {
        if (logMessage.length() > 0) {
            Antony.getLogger().info(logMessage.toString());
        }
    }

    /**
     * Captures context from the reaction event.
     * Keep 'name' as the raw event-provided value for debugging/logging.
     * The ReactionManager should set 'canonicalKey' before calling perform().
     */
    public void setVariables(MessageReactionAddEvent event) {
        this.name = event.getEmoji().getName();
        this.emote = event.getEmoji();
        this.guild = event.getGuild();
        this.message = event.retrieveMessage().complete();
        this.responseChannel = message.getChannel().asGuildMessageChannel();
        this.reactor = event.getMember();
    }

    // --------------------------------------------------
    // Helpers
    // --------------------------------------------------

    /**
     * Returns the key used for permission checks.
     * Prefers 'canonicalKey' (provided by ReactionManager) and falls back to 'name'.
     */
    private String getEffectivePermissionKey() {
        return (canonicalKey != null && !canonicalKey.isBlank()) ? canonicalKey : name;
    }

    // --------------------------------------------------
    // Getters & Setters
    // --------------------------------------------------

    public Boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(Boolean privileged) {
        this.privileged = privileged;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCanonicalKey() {
        return canonicalKey;
    }

    /**
     * Set by ReactionManager before perform() is called.
     * Example values: "🕵", "speaker", "redflag"
     */
    public void setCanonicalKey(String canonicalKey) {
        this.canonicalKey = canonicalKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Emoji getEmote() {
        return emote;
    }

    public void setEmote(Emoji emote) {
        this.emote = emote;
    }

    public Guild getGuild() {
        return guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public GuildMessageChannel getResponseChannel() {
        return responseChannel;
    }

    public void setResponseChannel(TextChannel responseChannel) {
        this.responseChannel = responseChannel;
    }

    public Member getReactor() {
        return reactor;
    }

    public void setReactor(Member reactor) {
        this.reactor = reactor;
    }

    public StringBuilder getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(StringBuilder logMessage) {
        this.logMessage = logMessage;
    }
}
