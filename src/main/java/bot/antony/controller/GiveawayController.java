package bot.antony.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.commands.giveaway.Giveaway;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * Controller to start, monitor and end {@link bot.antony.commands.giveaway.Giveaway Giveaways}
 *
 * @since  7.6.0
 * @author deso85
 */
public class GiveawayController {

	private List<Giveaway> giveaways = new ArrayList<Giveaway>();
	private String gaListFileName = "antony.giveaways.json";
	
	/**
     * Constructs a new GiveawayController instance, which can be used to start, monitor and end {@link bot.antony.commands.giveaway.Giveaway giveaways}.
     */
	public GiveawayController() {
		Antony.getLogger().info("Created giveaway controller.");
	}
	
	/**
     * Starts a {@link java.lang.Thread Thread} which checks every 15 seconds
     * if {@link bot.antony.commands.giveaway.Giveaway Giveaways} ended and
     * updates the related {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     * inside the Discord message if necessary.
     * 
     * @param  jda
     *         The {@link net.dv8tion.jda.api.JDA JDA} instance is needed to get
     *         necessary objects like {@link net.dv8tion.jda.api.entities.Guild Guilds}
     *         where {@link bot.antony.commands.giveaway.Giveaway Giveaways} take place
     */
	public void run(JDA jda) {
		load();
		Thread timerThread = new Thread() {
			public void run() {
				while(jda.getPresence().getStatus() == OnlineStatus.ONLINE) {
					try {
						if(giveaways.size() > 0) {
							for(Giveaway giveaway : new ArrayList<Giveaway>(giveaways)) {
								if(giveaway.hasEnded()) {
									
									Guild guild = jda.getGuildById(giveaway.getGuildID());
									String winner = getWinner(guild, giveaway.getChanID(), giveaway.getMsgID(), giveaway.getWinCount());
									String sponsorName = giveaway.getSponsorName();
									String sponsorAvatar = null;
									String replyMessage = "Das Giveaway wurde beendet, " + sponsorName + " ist aber nicht mehr auf dem Server... 😲";
									if(guild.getMemberById(giveaway.getSponsorID()) != null) {
										User sponsor = jda.getUserById(giveaway.getSponsorID());
										sponsorName = sponsor.getName();
										sponsorAvatar = sponsor.getAvatarUrl();
										replyMessage = guild.getMember(sponsor).getAsMention() + " dein Giveaway wurde beendet und "
												+ ((giveaway.getWinCount() > 1) ? "die" : "der")
												+ " Gewinner ausgelost.";
									}
									EmbedBuilder eb = getEmbedBuilder(sponsorName, sponsorAvatar, giveaway.getDescription(), giveaway.getWinCount(), giveaway.getGaEndEpochSeconds());
									eb.addField("Gewinner", winner, false);
									Message gaMessage = guild.getTextChannelById(giveaway.getChanID()).retrieveMessageById(giveaway.getMsgID()).complete();
									gaMessage.editMessageEmbeds(eb.build()).queue();
									gaMessage.reply(replyMessage).queue();
									
									removeGA(giveaway);
								}
								Thread.sleep(5000);
							}
						}
						Thread.sleep(15000); //15sec
					} catch (InterruptedException e) {
						Antony.getLogger().error("Wasn't able to put Thread asleep.", e);
					}
				}
			}
		};
		timerThread.start();
	}
	
	/**
	* Adds a {@link bot.antony.commands.giveaway.Giveaway Giveaway} and posts a
	* {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} in a specified
	* {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel TextChannel}
	* with the details
	*
	* @param  sponsor
	*         The sponsor of the giveaway as an {@link net.dv8tion.jda.api.entities.User User} object.
	* @param  description
	*         The giveaways description.
	* @param  channel
	*         The {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel TextChannel} to post
	*         the {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} in.
	* @param  runtimeMin
	*         Specifies how long the giveaway will run in minutes.
	* @param  winCount
	*         The amount of people who can win the giveaway.
	*/
	public void addGA(User sponsor, String description, TextChannel channel, int runtimeMin, int winCount) {
		EmbedBuilder eb = getEmbedBuilder(sponsor.getName(),
				sponsor.getAvatarUrl(),
				description,
				winCount,
				(Instant.now().getEpochSecond() + (runtimeMin*60)));
		eb.addField("Teilnahme", "Reagiere mit 🎁, um am Giveaway teilnehmen zu können.", false);
		
		channel.sendMessageEmbeds(eb.build()).queue(msg -> {
			Giveaway ga = new Giveaway(sponsor.getId(),
					sponsor.getName(),
					description,
					msg,
					runtimeMin,
					winCount);
			addGA(ga);
			msg.addReaction(Emoji.fromUnicode("🎁")).queue();
		});
	}
	
	/**
	* Adds a {@link bot.antony.commands.giveaway.Giveaway Giveaway} to the list of active giveaways
	* and stores it.
	*
	* @param  giveaway
	*/
	public void addGA(Giveaway giveaway) {
		giveaways.add(giveaway);
		save();
	}
	
	/**
	* Removes a {@link bot.antony.commands.giveaway.Giveaway Giveaway} from the list of active
	* giveaways and stores it.
	*
	* @param  giveaway
	*/
	public void removeGA(Giveaway giveaway) {
		giveaways.remove(giveaway);
		save();
	}
	
	/**
	* Loads the list of active {@link bot.antony.commands.giveaway.Giveaway Giveaways}.
	*/
	@SuppressWarnings("unchecked")
	public void load() {
		giveaways = (List<Giveaway>) Utils.loadJSONData(gaListFileName, new TypeReference<List<Giveaway>>(){}, giveaways);
	}
	
	/**
	* Saves the list of active {@link bot.antony.commands.giveaway.Giveaway Giveaways}.
	*/
	public void save() {
		Utils.saveJSONData(gaListFileName, giveaways);
	}
	
	/**
	 * Prepares and returns an {@link net.dv8tion.jda.api.EmbedBuilder EmbedBuilder}
	 * with {@link bot.antony.commands.giveaway.Giveaway Giveaway} data.
	 * 
	 * @param sponsorName
	 *        The sponsors name.
	 * @param sponsorAvatar
	 *        The sponsors avatar URL.
	 * @param description
	 *        The giveaways description.
	 * @param winCount
	 *        The amount of people who can win the giveaway.
	 * @param endEpochSecond
	 *        The epoch second (seconds after 1970-01-01) of the time when the giveaway ends.
	 * @return {@link net.dv8tion.jda.api.EmbedBuilder EmbedBuilder}
	 */
	private EmbedBuilder getEmbedBuilder(String sponsorName, String sponsorAvatar, String description, int winCount, long endEpochSecond) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Antony.getBaseColor());
		eb.setAuthor(sponsorName, null, sponsorAvatar);
		eb.setTitle("Giveaway von " + sponsorName);
		eb.setDescription(description);
		eb.addField("Gewinn-Chancen", winCount + "", false);
		eb.addField(((endEpochSecond > Instant.now().getEpochSecond()) ? "Endet" : "Endete"), "<t:" + endEpochSecond + ":R>", false);
		eb.setFooter("Das Giveaway wird ausschließlich durch den Veranstalter verantwortet.");
		return eb;
	}
	
	/**
	 * Returns a String which includes all winner of the {@link bot.antony.commands.giveaway.Giveaway Giveaway}.
	 * 
	 * @param guild
	 *        The {@link net.dv8tion.jda.api.entities.Guild Guild} in which the giveaway takes place.
	 * @param chanID
	 *        The channel ID in which the giveaway message has been posted.
	 * @param messageID
	 *        The ID of the message related to the giveaway.
	 * @param winCount
	 *        The amount of people who can win the giveaway.
	 * @return winner
	 *         as String
	 */
	public String getWinner(Guild guild, long chanID, long messageID, int winCount) {
		StringBuilder winner = new StringBuilder();
		
		List<User> usrListA = guild.getTextChannelById(chanID).retrieveMessageById(messageID).complete().getReaction(Emoji.fromUnicode("🎁")).retrieveUsers().complete();
		List<User> usrListB = new ArrayList<User>();
		for(User usr : usrListA) {
			if(guild.getMember(usr) != null
					&& !usr.isBot()) {
				usrListB.add(usr);
			}
		}
		usrListA = new ArrayList<>(usrListB);
		usrListB = new ArrayList<>();
		Random rand = new Random();
		for (int i = 0; i < winCount; i++) {
			if(usrListA.size() > 0) {
				int randomIndex = rand.nextInt(usrListA.size());
				usrListB.add(usrListA.get(randomIndex));
		        usrListA.remove(randomIndex);
			}
		}
		
		if(usrListB.size() > 0) {
			for (int i = 0; i < usrListB.size(); i++) {
				winner.append(usrListB.get(i).getAsMention());
				if(i < usrListB.size()-1) {
					winner.append(", ");
				}
			}
		} else {
			winner.append("Niemand 😢");
		}
		
		return winner.toString();
	}
}
