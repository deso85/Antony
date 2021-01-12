package bot.antony.commands;

import org.joda.time.DateTime;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class PerformGiveaway implements ServerCommand {

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		final Guild guild = channel.getGuild();
		TextChannel giveawayChannel = channel;

		// If giveaway will be started in another channel
		if(message.getMentionedChannels().size() > 0) {
			giveawayChannel = message.getMentionedChannels().get(0);
		}
		
		if(userMessage.length >= 4) {

			switch (userMessage[1].toLowerCase()) {
			case "end": // Ends a giveaway which wasn't initiated by Antony
				
				String[] giveawayLink = userMessage[2].split("/");
				if(giveawayLink.length >= 3) {
					giveawayChannel = guild.getTextChannelById(giveawayLink[giveawayLink.length-2]);
					//Message giveawayMessage = giveawayChannel.retrieveMessageById(giveawayLink[giveawayLink.length-1]);
				}
				
				break;
			default: // Should start a new giveaway
				
				
				
				break;
			}
		} else {
			channel.sendMessage(printHelp()).queue();
		}
		
		//giveawayChannel.sendMessage(giveawayChannel.getAsMention()).queue();
		
		//new DateTime();
		DateTime datetime = new DateTime(DateTime.now());
		System.out.println(datetime);
		
	}
	
	public String printHelp() {
		StringBuilder helpText = new StringBuilder();
		helpText.append("So nutzt du " + Antony.getCmdPrefix() + "giveaway:\n");

		helpText.append("***"+ Antony.getCmdPrefix() + "giveaway end <Nachrichtenlink> <Emoji>*** - Beendet ein Giveaway, dass nicht über Antony gestartet wurde.\n");
		helpText.append("***Beispiel:*** *"+ Antony.getCmdPrefix() + "giveaway end https://discord.com/channels/375031723601297409/605451097699647665/798303589763252224 :tada:*\n\n");
		
		helpText.append("***"+ Antony.getCmdPrefix() + "giveaway <Laufzeit> <Anzahl Gewinner> <Preis + Beschreibung> [Kanal]*** - Startet ein Giveaway.\n");
		helpText.append("***Beispiel:*** *"+ Antony.getCmdPrefix() + "giveaway 3d 1 Der Gewinner bekommt eine Lasius niger Kolonie aus dem Schwarmflug 2020. #giveaways*\n\n");
		return helpText.toString();
	}

}
