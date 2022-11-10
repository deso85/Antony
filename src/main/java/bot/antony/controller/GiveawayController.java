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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * This controller controls giveaways
 */
public class GiveawayController {

	private List<Giveaway> giveaways = new ArrayList<Giveaway>();
	private String gaListFileName = "antony.giveaways.json";
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public GiveawayController() {
		Antony.getLogger().info("Created giveaway controller.");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public void run(JDA jda) {
		load();
		Thread timerThread = new Thread() {
			public void run() {
				while(jda.getPresence().getStatus() == OnlineStatus.ONLINE) {
					try {
						if(giveaways.size() > 0) {
							List<Giveaway> gas = new ArrayList<Giveaway>(giveaways);
							for(Giveaway ga : gas) {
								if(ga.hasEnded()) {
									
									List<User> usrListA = jda.getGuildById(ga.getGuildID()).getTextChannelById(ga.getChanID()).retrieveMessageById(ga.getMsgID()).complete().getReaction(Emoji.fromUnicode("🎁")).retrieveUsers().complete();
									List<User> usrListB = new ArrayList<User>();
									for(User usr : usrListA) {
										if(jda.getGuildById(ga.getGuildID()).getMember(usr) != null
												&& !usr.isBot()) {
											usrListB.add(usr);
										}
									}
									usrListA = new ArrayList<>(usrListB);
									usrListB = new ArrayList<>();
									Random rand = new Random();
									for (int i = 0; i < ga.getWinCount(); i++) {
										if(usrListA.size() > 0) {
											int randomIndex = rand.nextInt(usrListA.size());
											usrListB.add(usrListA.get(randomIndex));
									        usrListA.remove(randomIndex);
										}
									}
									StringBuilder winner = new StringBuilder();
									for (int i = 0; i < usrListB.size(); i++) {
										winner.append(usrListB.get(i).getAsMention());
										if(i < usrListB.size()-1) {
											winner.append(", ");
										}
									}
									if(winner.length() == 0) {
										winner.append("Niemand 😢");
									}
									
									jda.getGuildById(ga.getGuildID()).getTextChannelById(ga.getChanID()).retrieveMessageById(ga.getMsgID()).queue(msg -> {
										String sponsorName = ga.getSponsorName();
										String sponsorAvatar = null;
										if(msg.getGuild().getMemberById(ga.getSponsorID()) != null) {
											sponsorName = jda.getUserById(ga.getSponsorID()).getName();
											sponsorAvatar = jda.getUserById(ga.getSponsorID()).getAvatarUrl();
										}
										EmbedBuilder eb = new EmbedBuilder();
										eb.setColor(Antony.getBaseColor());
										eb.setAuthor(sponsorName, null, sponsorAvatar);
										eb.setTitle("Giveaway von " + sponsorName);
										eb.setDescription(ga.getDescription());
										eb.addField("Endete", "<t:" + ga.getGaEndEpochSeconds() + ":R>", false);
										eb.addField("Gewinner", winner.toString(), false);
										eb.setFooter("Das Giveaway wird ausschließlich durch den Veranstalter verantwortet.");
										msg.editMessageEmbeds(eb.build()).queue();
										if(msg.getGuild().getMemberById(ga.getSponsorID()) != null) {
											msg.reply(msg.getGuild().getMemberById(ga.getSponsorID()).getAsMention() + " dein Giveaway wurde beendet und der/die Gewinner ausgelost.").queue();
										} else {
											msg.reply("Das Giveaway wurde beendet, " + sponsorName + " ist aber nicht mehr auf dem Server... 😲").queue();
										}
									});
									
									removeGA(ga);
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
	
	public void addGA(User sponsor, String description, TextChannel channel, int runtimeMin, int winCount) {
		channel.sendMessageEmbeds(getInitEmbed(sponsor, description, runtimeMin, winCount)).queue(msg -> {
			addGA(new Giveaway(sponsor.getId(), sponsor.getName(), description, msg, runtimeMin, winCount));
			msg.addReaction(Emoji.fromUnicode("🎁")).queue();
		});
	}
	
	public void addGA(Giveaway ga) {
		giveaways.add(ga);
		save();
	}
	
	public void removeGA(Giveaway ga) {
		giveaways.remove(ga);
		save();
	}
	
	@SuppressWarnings("unchecked")
	public void load() {
		giveaways = (List<Giveaway>) Utils.loadJSONData(gaListFileName, new TypeReference<List<Giveaway>>(){}, giveaways);
	}
	
	public void save() {
		Utils.saveJSONData(gaListFileName, giveaways);
	}
	
	private MessageEmbed getInitEmbed(User sponsor, String description, int runtimeMin, int winCount) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Antony.getBaseColor());
		eb.setTitle("Giveaway von " + sponsor.getName());
		eb.setDescription(description);
		eb.addField("Gewinn-Chancen", winCount + "", false);
		eb.addField("Endet", "<t:" + (Instant.now().getEpochSecond() + (runtimeMin*60)) + ":R>", false);
		eb.addField("Teilnahme", "Reagiere mit 🎁, um am Giveaway teilnehmen zu können.", false);
		eb.setAuthor(sponsor.getName(), null, sponsor.getAvatarUrl());
		eb.setFooter("Das Giveaway wird ausschließlich durch den Veranstalter verantwortet.");
		return eb.build();
	}
}
