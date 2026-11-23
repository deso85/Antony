package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.controller.GuildController;
import bot.antony.guild.GuildData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class GuildCmd extends ServerCommand {
	
	private GuildMessageChannel channel;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public GuildCmd() {
		super();
		this.privileged = true;
		this.name = "guild";
		this.description = "Mit diesem Befehl kann der Discord Server verwaltet werden.";
		this.shortDescription = "Befehl zur Verwaltung des Discord Servers.";
		this.example = "welcomechan #welcome";
		this.cmdParams.put("logchan (#logChannel)", "Gibt den aktuellen Log-Kanal aus oder setzt diesen.");
		this.cmdParams.put("welcomechan (#welcomeChannel)", "Gibt den aktuellen Welcome-Kanal aus oder setzt diesen.");
		this.cmdParams.put("activationchan (#activationChannel)", "Gibt den aktuellen Activation-Kanal aus oder setzt diesen.");
		this.cmdParams.put("exitchan (#exitChannel)", "Gibt den aktuellen Exit-Kanal aus oder setzt diesen.");
		this.cmdParams.put("commandchan (#commandChannel)", "Gibt den aktuellen Kanal für Commands aus oder setzt diesen.");
		this.cmdParams.put("rolechan (#roleChannel)", "Gibt den aktuellen Kanal für die Rollenvergabe per Reaktion aus oder setzt diesen. Verlässt ein User den Server, werden seine Reaktionen in diesem Kanal entfernt.");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		this.channel = channel;
		
		if (userMessage.length > 1) {
			switch (userMessage[1].toLowerCase()) {
				case "logchan":
					if(message.getMentions().getChannels(TextChannel.class).size() > 0) {
						setLogChan(message.getMentions().getChannels(TextChannel.class).get(0));
					} else {
						printLogChan(message.getGuild());
					}
					break;
					
				case "welcomechan":
					if(message.getMentions().getChannels(TextChannel.class).size() > 0) {
						setWelcomeChan(message.getMentions().getChannels(TextChannel.class).get(0));
					} else {
						printWelcomeChan(message.getGuild());
					}
					break;
				
				case "activationchan":
					if(message.getMentions().getChannels(TextChannel.class).size() > 0) {
						setActivationChan(message.getMentions().getChannels(TextChannel.class).get(0));
					} else {
						printActivationChan(message.getGuild());
					}
					break;
				case "exitchan":
					if(message.getMentions().getChannels(TextChannel.class).size() > 0) {
						setExitChan(message.getMentions().getChannels(TextChannel.class).get(0));
					} else {
						printExitChan(message.getGuild());
					}
					break;
				case "commandchan":
					if(message.getMentions().getChannels(TextChannel.class).size() > 0) {
						setCommmandChan(message.getMentions().getChannels(TextChannel.class).get(0));
					} else {
						printCommandChan(message.getGuild());
					}
					break;
				case "rolechan":
					if(message.getMentions().getChannels(TextChannel.class).size() > 0) {
						setRoleManagementChan(message.getMentions().getChannels(TextChannel.class).get(0));
					} else {
						printRoleManagementChan(message.getGuild());
					}
					break;
					
				default:
					printHelp(channel);
					break;
			}
		} else {
			printHelp(channel);
		}
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	//LogChannel
	private void setLogChan(TextChannel logChan) {
		GuildData guildData = Antony.getGuildController().loadGuildData(logChan.getGuild());
		guildData.setLogChannelID(logChan.getIdLong());
		Antony.getGuildController().saveGuildData(guildData, logChan.getGuild());
		channel.sendMessage("Log Channel gesetzt.").queue();
		Antony.getLogger().info("Log channel of guild " + guildData.toString() + " set to " + logChan.getAsMention());
	}
	
	private void printLogChan(Guild guild) {
		GuildController guildController = Antony.getGuildController();
		if(guildController.getLogChannel(guild) != null) {
			channel.sendMessage("Log Channel ist: " + guildController.getLogChannel(guild).getAsMention()).queue();
		} else {
			channel.sendMessage("Es ist kein Log Channel gesetzt.").queue();
		}
	}
	
	//Welcome Channel
	private void setWelcomeChan(TextChannel welcomeChan) {
		GuildData guildData = Antony.getGuildController().loadGuildData(welcomeChan.getGuild());
		guildData.setWelcomeChannelID(welcomeChan.getIdLong());
		Antony.getGuildController().saveGuildData(guildData, welcomeChan.getGuild());
		channel.sendMessage("Welcome Channel gesetzt.").queue();
		Antony.getLogger().info("Welcome channel of guild " + guildData.toString() + " set to " + welcomeChan.getAsMention());
	}
	
	private void printWelcomeChan(Guild guild) {
		GuildController guildController = Antony.getGuildController();
		if(guildController.getWelcomeChannel(guild) != null) {
			channel.sendMessage("Welcome Channel ist: " + guildController.getWelcomeChannel(guild).getAsMention()).queue();
		} else {
			channel.sendMessage("Es ist kein Welcome Channel gesetzt.").queue();
		}
	}
	
	//Activation Channel
	private void setActivationChan(TextChannel activationChan) {
		GuildData guildData = Antony.getGuildController().loadGuildData(activationChan.getGuild());
		guildData.setActivationRulesChannelID(activationChan.getIdLong());
		Antony.getGuildController().saveGuildData(guildData, activationChan.getGuild());
		channel.sendMessage("Channel für Aktivierungs-Regeln gesetzt.").queue();
		Antony.getLogger().info("Activation rules channel of guild " + guildData.toString() + " set to " + activationChan.getAsMention());
	}
	
	private void printActivationChan(Guild guild) {
		GuildController guildController = Antony.getGuildController();
		if(guildController.getActivationRulesChannel(guild) != null) {
			channel.sendMessage("Channel für Aktivierungs-Regeln ist: " + guildController.getActivationRulesChannel(guild).getAsMention()).queue();
		} else {
			channel.sendMessage("Es ist kein Channel für Aktivierungs-Regeln gesetzt.").queue();
		}
	}
	
	//Exit Channel
	private void setExitChan(TextChannel exitChan) {
		GuildData guildData = Antony.getGuildController().loadGuildData(exitChan.getGuild());
		guildData.setExitChannelID(exitChan.getIdLong());
		Antony.getGuildController().saveGuildData(guildData, exitChan.getGuild());
		channel.sendMessage("Channel für Exit-Benachrichtigungen gesetzt.").queue();
		Antony.getLogger().info("Exit channel of guild " + guildData.toString() + " set to " + exitChan.getAsMention());
	}
	
	private void printExitChan(Guild guild) {
		GuildController guildController = Antony.getGuildController();
		if(guildController.getExitChannel(guild) != null) {
			channel.sendMessage("Channel für Exit-Benachrichtigungen ist: " + guildController.getExitChannel(guild).getAsMention()).queue();
		} else {
			channel.sendMessage("Es ist kein Channel für Exit-Benachrichtigungen gesetzt.").queue();
		}
	}
	
	//Command Channel
	private void setCommmandChan(TextChannel commandChan) {
		GuildData guildData = Antony.getGuildController().loadGuildData(commandChan.getGuild());
		guildData.setCommandsChannelID(commandChan.getIdLong());
		Antony.getGuildController().saveGuildData(guildData, commandChan.getGuild());
		channel.sendMessage("Channel für Commands gesetzt.").queue();
		Antony.getLogger().info("Command channel of guild " + guildData.toString() + " set to " + commandChan.getAsMention());
	}
	
	private void printCommandChan(Guild guild) {
		GuildController guildController = Antony.getGuildController();
		if(guildController.getCommandChannel(guild) != null) {
			channel.sendMessage("Channel für Commands ist: " + guildController.getCommandChannel(guild).getAsMention()).queue();
		} else {
			channel.sendMessage("Es ist kein Channel für Commands gesetzt.").queue();
		}
	}
	
	//Role Management Channel
	private void setRoleManagementChan(TextChannel roleChan) {
		GuildData guildData = Antony.getGuildController().loadGuildData(roleChan.getGuild());
		guildData.setRoleManagementChannelID(roleChan.getIdLong());
		Antony.getGuildController().saveGuildData(guildData, roleChan.getGuild());
		channel.sendMessage("Channel für die Rollenvergabe gesetzt.").queue();
		Antony.getLogger().info("Role management channel of guild " + guildData.toString() + " set to " + roleChan.getAsMention());
		
		Member self = roleChan.getGuild().getSelfMember();
		if(!self.hasPermission(roleChan, Permission.MESSAGE_HISTORY, Permission.MESSAGE_MANAGE)) {
			channel.sendMessage("Achtung: Mir fehlen in " + roleChan.getAsMention() + " die Berechtigungen *Nachrichtenverlauf lesen* und/oder *Nachrichten verwalten*. Ohne diese kann ich Reaktionen von Usern, die den Server verlassen, nicht entfernen.").queue();
		}
	}
	
	private void printRoleManagementChan(Guild guild) {
		GuildController guildController = Antony.getGuildController();
		if(guildController.getRoleManagementChannel(guild) != null) {
			channel.sendMessage("Channel für die Rollenvergabe ist: " + guildController.getRoleManagementChannel(guild).getAsMention()).queue();
		} else {
			channel.sendMessage("Es ist kein Channel für die Rollenvergabe gesetzt.").queue();
		}
	}
}
