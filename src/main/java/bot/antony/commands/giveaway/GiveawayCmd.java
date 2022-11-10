package bot.antony.commands.giveaway;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class GiveawayCmd extends ServerCommand {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public GiveawayCmd() {
		super();
		this.privileged = false;
		this.name = "giveaway";
		this.description = "Mit diesem Befehl lassen sich Giveaways starten. Der Bot unterstützt bei der Anlage des Giveaways.";
		this.shortDescription = "Befehl zur starten eines Giveaways.";
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		String[] userMessage = message.getContentDisplay().split(" ");
		if(userMessage.length >= 3) {
		
			switch (userMessage[1].toLowerCase()) {
			case "reroll":
				if(member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
					
					String[] giveawayLink = userMessage[2].split("/");
					if(giveawayLink.length >= 3) {
						String gaChannelId = giveawayLink[giveawayLink.length-2];
						String gaMessageId = giveawayLink[giveawayLink.length-1];
						int gaWinnerCount = 1;
						if(userMessage.length >= 4) {
							try {
								gaWinnerCount = Integer.parseInt(userMessage[3]);
							}
							catch (NumberFormatException e)
							{
								e.printStackTrace();
							}
						}
						
						TextChannel gaChannel = channel.getGuild().getTextChannelById(gaChannelId);
						Message gaMessage = gaChannel.retrieveMessageById(gaMessageId).complete();
						List<User> usrListA = gaMessage.getReaction(Emoji.fromUnicode("🎁")).retrieveUsers().complete();
						List<User> usrListB = new ArrayList<User>();
						for(User usr : usrListA) {
							if(channel.getGuild().getMember(usr) != null
									&& !usr.isBot()) {
								usrListB.add(usr);
							}
						}
						usrListA = new ArrayList<>(usrListB);
						usrListB = new ArrayList<>();
						Random rand = new Random();
						for (int i = 0; i < gaWinnerCount; i++) {
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
						channel.sendMessage("Folgende User wurden neu ausgelost: " + winner.toString()).queue();
					}
					
				}
				break;
			default:
				printHelp(channel);
				break;
			}
			
		} else {
			Antony.getLogger().info("Giveaway dialogue started");
			message.reply(
				"Was willst du verschenken und welche Bedingungen gibt es? *(Freitext)"
				+ "\n`stop` beendet den Dialog*")
				.queue(msg -> {
					message.getJDA().addEventListener(
							new GiveawayStateMachine(channel, member.getUser()));
				});
		}
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------

}