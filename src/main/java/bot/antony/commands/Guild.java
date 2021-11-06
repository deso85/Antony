package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.controller.GuildController;
import bot.antony.guild.GuildData;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Guild implements ServerCommand {
	
	TextChannel channel;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		GuildController guildController = Antony.getGuildController();
		GuildData guildData = guildController.loadGuildData(message.getGuild());
		this.channel = channel;
		int counter = 0;
		
		String[] userMessage = message.getContentDisplay().split(" ");
		if (userMessage.length > 1) {
			StringBuilder returnMessage = new StringBuilder();
			
			switch (userMessage[1].toLowerCase()) {
				case "logchan":
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
					
				case "welcomechan":
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
				
				case "adminrole":
					if(message.getMentionedRoles().size()> 0) {
						if(guildData.addAdminRole(message.getMentionedRoles().get(0).getName())) {
							returnMessage.append("Administrative Rolle hinzugefügt.");
							Antony.getLogger().info("Admin role \"" + message.getMentionedRoles().get(0).getName() + "\" added to guild " + guildData.toString());
						} else {
							guildData.removeAdminRole(message.getMentionedRoles().get(0).getName());
							returnMessage.append("Administrative Rolle entfernt.");
							Antony.getLogger().info("Admin role \"" + message.getMentionedRoles().get(0).getName() + "\" removed from guild " + guildData.toString());
						}
						guildController.saveGuildData(guildData, channel.getGuild());
						
					} else {
						if(guildData.getAdminRoles().size() > 0) {
							returnMessage.append("Folgende Rollen wurden als administrative Rollen konfiguriert:\n");
							counter = 0;
							for(String roleName : guildData.getAdminRoles()) {
								returnMessage.append("@" + roleName);
								counter++;
								if(counter < guildData.getAdminRoles().size()) {
									returnMessage.append(", ");
								}
							}
						} else {
							returnMessage.append("Es sind keine administrativen Rollen konfiguriert.");
						}
					}
					break;
					
				case "modrole":
					if(message.getMentionedRoles().size()> 0) {
						if(guildData.addModRole(message.getMentionedRoles().get(0).getName())) {
							returnMessage.append("Moderative Rolle hinzugefügt.");
							Antony.getLogger().info("Mod role \"" + message.getMentionedRoles().get(0).getName() + "\" added to guild " + guildData.toString());
						} else {
							guildData.removeModRole(message.getMentionedRoles().get(0).getName());
							returnMessage.append("Moderative Rolle entfernt.");
							Antony.getLogger().info("Mod role \"" + message.getMentionedRoles().get(0).getName() + "\" removed from guild " + guildData.toString());
						}
						guildController.saveGuildData(guildData, channel.getGuild());
						
					} else {
						if(guildData.getModRoles().size() > 0) {
							returnMessage.append("Folgende Rollen wurden als moderative Rollen konfiguriert:\n");
							counter = 0;
							for(String roleName : guildData.getModRoles()) {
								returnMessage.append("@" + roleName);
								counter++;
								if(counter < guildData.getModRoles().size()) {
									returnMessage.append(", ");
								}
							}
						} else {
							returnMessage.append("Es sind keine moderativen Rollen konfiguriert.");
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
	
	private void printHelp() {
		channel.sendMessage("Benutzung: " + Antony.getCmdPrefix() + "guild (logchan | welcomechan | adminrole | modrole) [#TextChannel | @role]").complete();
	}
}
