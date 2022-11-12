package bot.antony.commands.giveaway;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Command to interact with the {@link bot.antony.controller.GiveawayController GiveawayController}.
 *
 * @since  7.6.0
 * @author deso85
 */
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
		this.example = "reroll https://discord.com/channels/375031723601297409/605451097699647665/1040599569970516039";
		this.cmdParams.put("`ohne`", "Startet den Giveaway-Dialog, um ein neues Giveaway anzulegen");
		this.cmdParams.put("reroll (Link to Message) [Amount of Winners]", "Lost Gewinner neu aus, benötigt administrative Rechte");
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
						String winner = Antony.getGiveawayController().getWinner(channel.getGuild(), gaChannel.getIdLong(), gaMessage.getIdLong(), gaWinnerCount);
						channel.sendMessage("Folgende User wurden neu ausgelost: " + winner).queue();
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