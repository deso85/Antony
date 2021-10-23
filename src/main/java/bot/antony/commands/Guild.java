package bot.antony.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.controller.GuildController;
import bot.antony.guild.GuildData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Guild implements ServerCommand {
	
	TextChannel channel;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		GuildController guildController = Antony.getGuildController();
		GuildData guildData = guildController.loadGuildData(message.getGuild());
		List<String> allowedRoles = new ArrayList<>(Arrays.asList("Admin", "Soldat"));	//Roles which may use the command
		this.channel = channel;
		
		if(Utils.memberHasRole(member, allowedRoles)) {
			String[] userMessage = message.getContentDisplay().split(" ");
			if (userMessage.length > 1) {
				StringBuilder returnMessage = new StringBuilder();
				
				switch (userMessage[1].toLowerCase()) {
					case "log":
						if(message.getMentionedChannels().size() > 0) {
							guildData.setLogChannelID(message.getMentionedChannels().get(0).getIdLong());
							guildController.saveGuildData(guildData, channel.getGuild());
							returnMessage.append("Log Channel gesetzt.");
							Antony.getLogger().info("Log channel of guild " + guildData.toString() + " set to " + message.getMentionedChannels().get(0).getAsMention());
						} else {
							if(guildController.getLogChannel(message.getGuild()) != null) {
								returnMessage.append("Log Channel ist: " + guildController.getLogChannel(message.getGuild()).getAsMention());
							} else {
								returnMessage.append("Es ist kein Log Channel gesetzt.");
							}
						}
						break;
						
					case "welcome":
						if(message.getMentionedChannels().size() > 0) {
							guildData.setWelcomeChannelID(message.getMentionedChannels().get(0).getIdLong());
							guildController.saveGuildData(guildData, channel.getGuild());
							returnMessage.append("Welcome Channel gesetzt.");
							Antony.getLogger().info("Welcome channel of guild " + guildData.toString() + " set to " + message.getMentionedChannels().get(0).getAsMention());
						} else {
							if(guildController.getWelcomeChannel(message.getGuild()) != null) {
								returnMessage.append("Welcome Channel ist: " + guildController.getWelcomeChannel(message.getGuild()).getAsMention());
							} else {
								returnMessage.append("Es ist kein Welcome Channel gesetzt.");
							}
						}
						break;
						
					default:
						printHelp();
						break;
				}
				
				if(returnMessage.length() > 0) {
					channel.sendMessage(returnMessage.toString()).complete();
				}
			} else {
				printHelp();
			}
		}
	}
	
	private void printHelp() {
		channel.sendMessage("Benutzung: " + Antony.getCmdPrefix() + "guild (log | welcome) [#TextChannel]").complete();
	}
}
