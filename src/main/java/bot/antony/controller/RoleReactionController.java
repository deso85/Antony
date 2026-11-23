package bot.antony.controller;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import bot.antony.Antony;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

/**
 * Keeps the reactions in the role management channel of a guild in sync with its members.
 * <ul>
 * <li>When a user leaves, all of their reactions in the role management channel are removed.</li>
 * <li>Once per night all reactions are compared with the current member list and reactions
 * of users who are no longer members are removed (catches users who left while the bot was offline).</li>
 * </ul>
 * A reaction is only removed during the nightly sync if the complete member list could be loaded
 * and Discord additionally confirms for the single user that they are no longer a member.
 */
public class RoleReactionController {

	/** Maximum number of messages in the role management channel that are checked. */
	private static final int MESSAGE_LIMIT = 100;
	/** Time of the nightly sync. */
	private static final LocalTime SYNC_TIME = LocalTime.of(3, 30);
	private static final ZoneId SYNC_ZONE = ZoneId.of("Europe/Berlin");
	/** Retries if the member list seems to be incomplete. */
	private static final int MEMBER_LOAD_ATTEMPTS = 3;
	private static final long MEMBER_LOAD_RETRY_DELAY_MS = 60_000;

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public RoleReactionController() {
		super();
	}

	// --------------------------------------------------
	// Leave event
	// --------------------------------------------------
	/**
	 * Removes all reactions of the given (leaving) user in the role management channel.
	 * Runs asynchronously.
	 */
	public void removeReactionsOfUser(Guild guild, User user) {
		TextChannel roleChannel = Antony.getGuildController().getRoleManagementChannel(guild);
		if(roleChannel == null || !hasRequiredPermissions(guild, roleChannel)) {
			return;
		}

		roleChannel.getIterableHistory().takeAsync(MESSAGE_LIMIT).thenAccept(messages -> {
			// The user might have rejoined in the meantime - do not touch their reactions then
			if(guild.getMemberById(user.getIdLong()) != null) {
				Antony.getLogger().info("User " + user.getName() + " (" + user.getId() + ") is a member of guild "
						+ guild.getName() + " again. Reactions in role management channel are kept.");
				return;
			}
			int requested = 0;
			for(Message message : messages) {
				for(MessageReaction reaction : message.getReactions()) {
					// Discord ignores the request if the user did not react with this emoji
					message.removeReaction(reaction.getEmoji(), user).queue(
						null,
						error -> Antony.getLogger().debug("Could not remove reaction " + reaction.getEmoji().getFormatted()
								+ " of user " + user.getId() + " on message " + message.getId() + ": " + error.getMessage()));
					requested++;
				}
			}
			Antony.getLogger().info("Removing reactions of user " + user.getName() + " (" + user.getId() + ") in role management channel #"
					+ roleChannel.getName() + " of guild " + guild.getName() + " (" + messages.size() + " messages, " + requested + " reactions).");
		}).exceptionally(error -> {
			Antony.getLogger().error("Could not read history of role management channel #" + roleChannel.getName()
					+ " (" + roleChannel.getId() + ") of guild " + guild.getName() + ".", error);
			return null;
		});
	}

	// --------------------------------------------------
	// Nightly sync
	// --------------------------------------------------
	/**
	 * Starts the thread which runs the sync every night.
	 */
	public void run(JDA jda) {
		Thread syncThread = new Thread() {
			public void run() {
				while(jda.getPresence().getStatus() == OnlineStatus.ONLINE) {
					try {
						long waitMs = millisUntilNextSync();
						Antony.getLogger().info("Next role reaction sync in " + Duration.ofMillis(waitMs).toMinutes() + " minutes.");
						Thread.sleep(waitMs);
						syncAllGuilds(jda);
					} catch (InterruptedException e) {
						Antony.getLogger().info("Role reaction sync thread interrupted (likely during restart).");
						Thread.currentThread().interrupt();
						break;
					} catch (Exception e) {
						// Never let the thread die because of an unexpected error
						Antony.getLogger().error("Unexpected error during role reaction sync.", e);
					}
				}
			}
		};
		syncThread.setName("antony-role-reaction-sync");
		syncThread.setDaemon(true);
		Antony.registerTimerThread(syncThread);
		syncThread.start();
	}

	private long millisUntilNextSync() {
		ZonedDateTime now = ZonedDateTime.now(SYNC_ZONE);
		ZonedDateTime next = now.with(SYNC_TIME);
		if(!next.isAfter(now)) {
			next = next.plusDays(1);
		}
		return Duration.between(now, next).toMillis();
	}

	public void syncAllGuilds(JDA jda) throws InterruptedException {
		for(Guild guild : jda.getGuilds()) {
			syncGuild(guild);
		}
	}

	/**
	 * Compares all reactions in the role management channel with the members of the guild
	 * and removes reactions of users who are no longer members.
	 * Blocking - must not be called from a JDA callback thread.
	 */
	public void syncGuild(Guild guild) throws InterruptedException {
		TextChannel roleChannel = Antony.getGuildController().getRoleManagementChannel(guild);
		if(roleChannel == null) {
			return;
		}
		if(!hasRequiredPermissions(guild, roleChannel)) {
			return;
		}

		String logPrefix = "[Role reaction sync] Guild " + guild.getName() + " (" + guild.getId() + "): ";

		// 1) Load the complete member list - abort if it is not complete
		Set<Long> memberIds = loadCompleteMemberIds(guild, logPrefix);
		if(memberIds == null) {
			Antony.getLogger().warn(logPrefix + "Member list could not be loaded completely. Sync aborted, no reactions removed.");
			return;
		}

		// 2) Load messages of the role management channel
		List<Message> messages;
		try {
			messages = roleChannel.getIterableHistory().takeAsync(MESSAGE_LIMIT).get();
		} catch (Exception e) {
			Antony.getLogger().error(logPrefix + "Could not read history of role management channel #" + roleChannel.getName() + ". Sync aborted.", e);
			return;
		}

		// 3) Check all reacting users
		Map<Long, Boolean> confirmedLeft = new HashMap<>();	// user id -> confirmed as non-member
		int checkedReactions = 0;
		int removedReactions = 0;
		for(Message message : messages) {
			for(MessageReaction reaction : message.getReactions()) {
				checkedReactions++;
				for(User user : reaction.retrieveUsers()) {	// iterates over all pages (blocking)
					if(memberIds.contains(user.getIdLong())) {
						continue;
					}
					boolean left = confirmedLeft.computeIfAbsent(user.getIdLong(), id -> isConfirmedNonMember(guild, user, logPrefix));
					if(!left) {
						continue;
					}
					try {
						message.removeReaction(reaction.getEmoji(), user).complete();
						removedReactions++;
					} catch (Exception e) {
						Antony.getLogger().warn(logPrefix + "Could not remove reaction " + reaction.getEmoji().getFormatted()
								+ " of user " + user.getId() + " on message " + message.getId() + ": " + e.getMessage());
					}
				}
			}
		}

		long removedUsers = confirmedLeft.values().stream().filter(b -> b).count();
		Antony.getLogger().info(logPrefix + "Sync of #" + roleChannel.getName() + " finished. Checked " + messages.size() + " messages / "
				+ checkedReactions + " reactions, removed " + removedReactions + " reactions of " + removedUsers + " former members.");
	}

	/**
	 * Loads all members of the guild directly from Discord.
	 * @return the member IDs, or null if the list does not seem to be complete
	 */
	private Set<Long> loadCompleteMemberIds(Guild guild, String logPrefix) throws InterruptedException {
		for(int attempt = 1; attempt <= MEMBER_LOAD_ATTEMPTS; attempt++) {
			try {
				List<Member> members = guild.loadMembers().get();
				int expected = guild.getMemberCount();
				if(!members.isEmpty() && members.size() >= expected) {
					Set<Long> ids = new HashSet<>();
					for(Member member : members) {
						ids.add(member.getIdLong());
					}
					return ids;
				}
				Antony.getLogger().warn(logPrefix + "Loaded " + members.size() + " of " + expected + " members (attempt " + attempt + "/" + MEMBER_LOAD_ATTEMPTS + ").");
			} catch (Exception e) {
				Antony.getLogger().warn(logPrefix + "Loading members failed (attempt " + attempt + "/" + MEMBER_LOAD_ATTEMPTS + "): " + e.getMessage());
			}
			if(attempt < MEMBER_LOAD_ATTEMPTS) {
				TimeUnit.MILLISECONDS.sleep(MEMBER_LOAD_RETRY_DELAY_MS);
			}
		}
		return null;
	}

	/**
	 * Asks Discord directly (bypassing the cache) whether the user is still a member.
	 * Only returns true if Discord explicitly answers "unknown member".
	 */
	private boolean isConfirmedNonMember(Guild guild, User user, String logPrefix) {
		try {
			guild.retrieveMemberById(user.getIdLong()).useCache(false).complete();
			Antony.getLogger().warn(logPrefix + "User " + user.getName() + " (" + user.getId() + ") was missing in member list but is still a member. Reactions kept.");
			return false;
		} catch (ErrorResponseException e) {
			if(e.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER || e.getErrorResponse() == ErrorResponse.UNKNOWN_USER) {
				return true;
			}
			Antony.getLogger().warn(logPrefix + "Could not verify membership of user " + user.getId() + ": " + e.getMessage() + ". Reactions kept.");
			return false;
		} catch (Exception e) {
			Antony.getLogger().warn(logPrefix + "Could not verify membership of user " + user.getId() + ": " + e.getMessage() + ". Reactions kept.");
			return false;
		}
	}

	private boolean hasRequiredPermissions(Guild guild, TextChannel roleChannel) {
		if(guild.getSelfMember().hasPermission(roleChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_MANAGE)) {
			return true;
		}
		Antony.getLogger().warn("Missing permissions (VIEW_CHANNEL, MESSAGE_HISTORY, MESSAGE_MANAGE) in role management channel #"
				+ roleChannel.getName() + " (" + roleChannel.getId() + ") of guild " + guild.getName() + ". Reactions cannot be removed.");
		return false;
	}
}
