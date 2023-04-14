package bot.antony.commands;

import java.util.ArrayList;
import java.util.Map.Entry;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.guild.GuildData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class CommandCmd extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public CommandCmd() {
		super();
		this.privileged = true;
		this.name = "command";
		this.description = "Mit diesem Befehl lassen sich die Berechtigungen für Bot-Befehle ansehen und verwalten.";
		this.shortDescription = "Berechtigungsverwaltung für Bot-Befehle.";
		this.example = "show " + name;
		this.cmdParams.put("list", "Listet alle verfügbaren Befehle auf");
		this.cmdParams.put("show cmdName", "Zeigt die Berechtigungen eines Befehls");
		this.cmdParams.put("addrole cmdName @role", "Erteilt einer Rolle die Berechtigung zur Nutzung eines Befehls");
		this.cmdParams.put("addmember cmdName @member", "Erteilt einem Member die Berechtigung zur Nutzung eines Befehls");
		this.cmdParams.put("removerole cmdName @role", "Entfernt einer Rolle die Berechtigung zur Nutzung eines Befehls");
		this.cmdParams.put("removemember cmdName @member", "Entfernt einem Member die Berechtigung zur Nutzung eines Befehls");
		this.cmdParams.put("clear cmdName", "Entfernt alle Berechtigungen zur Nutzung eines Befehls");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		if (userMessage.length > 1) {
			switch (userMessage[1].toLowerCase()) {
			case "list":
				list(channel);
				break;
			case "show":
				if (userMessage.length > 2) {
					show(channel, userMessage[2].toLowerCase());
				} else {
					printHelp(channel);
				}
				break;
			case "addrole":
				if (userMessage.length > 3 && !message.getMentions().getRoles().isEmpty()) {
					addRole(channel, userMessage[2].toLowerCase(), message.getMentions().getRoles().get(0));
				} else {
					printHelp(channel);
				}
				break;
			case "addmember":
				if (userMessage.length > 3 && !message.getMentions().getMembers().isEmpty()) {
					addMember(channel, userMessage[2].toLowerCase(), message.getMentions().getMembers().get(0));
				} else {
					printHelp(channel);
				}
				break;
			case "removerole":
				if (userMessage.length > 3 && !message.getMentions().getRoles().isEmpty()) {
					removeRole(channel, userMessage[2].toLowerCase(), message.getMentions().getRoles().get(0));
				} else {
					printHelp(channel);
				}
				break;
			case "removemember":
				if (userMessage.length > 3 && !message.getMentions().getMembers().isEmpty()) {
					removeMember(channel, userMessage[2].toLowerCase(), message.getMentions().getMembers().get(0));
				} else {
					printHelp(channel);
				}
				break;
			case "clear":
				if (userMessage.length > 2) {
					clearCommandPermissions(channel, userMessage[2].toLowerCase());
				} else {
					printHelp(channel);
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

	public void list(GuildMessageChannel channel) {
		StringBuilder retVal = new StringBuilder();
		
		if(Antony.getCmdMan().getCommands().isEmpty()) {
			retVal.append("Es wurden keine Befehle registriert.");
		} else {
			retVal.append("Folgende Befehle sind verfügbar:\n");
			for(Entry<String, ServerCommand> entry : Antony.getCmdMan().getCommands().entrySet()) {
				retVal.append("**" + Antony.getCmdPrefix() + entry.getKey() + "**");
				if(entry.getValue().isPrivileged()) {
					retVal.append(" (Zugriffbeschränkung)");
				}
				if(entry.getValue().getShortDescription() != null && !entry.getValue().getShortDescription().isEmpty()) {
					retVal.append(" - " + entry.getValue().getShortDescription());
				}
				retVal.append("\n");
			}
		}
		channel.sendMessage(retVal.toString()).queue();
	}
	
	public void show(GuildMessageChannel channel, String cmdName) {
		if(hasCommand(channel, cmdName)) {
			StringBuilder retVal = new StringBuilder();
			retVal.append("**" + Antony.getCmdPrefix() + cmdName + "** - Berechtigungen:");
			if(!Antony.getCmdMan().getCommand(cmdName).isPrivileged()) {
				retVal.append("\nJeder kann den Befehl ausführen.");
			} else {
				GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
				ArrayList<Long> rolesToDelete = new ArrayList<Long>(); //if isn't available anymore
				ArrayList<Long> membersToDelete = new ArrayList<Long>(); //if isn't available anymore
				
				if(!hasElements(guildData.getCmdRoles(cmdName)) && !hasElements(guildData.getCmdMembers(cmdName))) {
					retVal.append("\nNur der Server-Owner und Server-Administratoren können den Befehl ausführen.");
				}
				if(hasElements(guildData.getCmdRoles(cmdName))) {
					retVal.append("\nBerechtigte Rollen: ");
					int roleCount = 1;
					for(Long roleId : guildData.getCmdRoles(cmdName)) {
						if(channel.getGuild().getRoleById(roleId) != null) {
							retVal.append(channel.getGuild().getRoleById(roleId).getAsMention());
							if(roleCount < guildData.getCmdRoles(cmdName).size()) {
								retVal.append(", ");
								roleCount++;
							}
						} else {
							rolesToDelete.add(roleId);
						}
					}
				}
				if(hasElements(guildData.getCmdMembers(cmdName))) {
					retVal.append("\nBerechtigte Member: ");
					int memberCount = 1;
					for(Long memberId : guildData.getCmdMembers(cmdName)) {
						if(channel.getGuild().getMemberById(memberId) != null) {
							retVal.append(channel.getGuild().getMemberById(memberId).getAsMention());
							if(memberCount < guildData.getCmdMembers(cmdName).size()) {
								retVal.append(", ");
								memberCount++;
							}
						} else {
							membersToDelete.add(memberId);
						}
					}
				}
				cleanCmdLists(channel.getGuild(), cmdName, rolesToDelete, membersToDelete);
			}
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private void addRole(GuildMessageChannel channel, String cmdName, Role role) {
		if(hasCommand(channel, cmdName)) {
			StringBuilder retVal = new StringBuilder();
			GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
			if(guildData.addCmdRole(cmdName, role.getIdLong())) {
				retVal.append("Die Rolle " + role.getAsMention() + " darf nun den Befehl verwenden.");
				Antony.getGuildController().saveGuildData(guildData, channel.getGuild());
			} else {
				retVal.append("Die Rolle " + role.getAsMention() + " hatte bereits die Berechtigung, um den Befehl zu verwenden.");
			}
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private void addMember(GuildMessageChannel channel, String cmdName, Member member) {
		if(hasCommand(channel, cmdName)) {
			StringBuilder retVal = new StringBuilder();
			if(Antony.getCmdMan().getCommand(cmdName).isPrivileged()) {
				GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
				if(guildData.addCmdMember(cmdName, member.getIdLong())) {
					retVal.append(member.getAsMention() + " darf nun den Befehl verwenden.");
					Antony.getGuildController().saveGuildData(guildData, channel.getGuild());
				} else {
					retVal.append(member.getAsMention() + " hatte bereits die Berechtigung, um den Befehl zu verwenden.");
				}
			} else {
				retVal.append("Den Befehl darf jeder verwenden, weshalb keine zusätzlichen Berechtigungen vergeben wurden.");
			}
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private void removeRole(GuildMessageChannel channel, String cmdName, Role role) {
		if(hasCommand(channel, cmdName)) {
			StringBuilder retVal = new StringBuilder();
			if(Antony.getCmdMan().getCommand(cmdName).isPrivileged()) {
				GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
				if(guildData.removeCmdRole(cmdName, role.getIdLong())) {
					retVal.append("Die Berechtigungen der Rolle " + role.getAsMention() + " wurden entfernt.");
					Antony.getGuildController().saveGuildData(guildData, channel.getGuild());
				} else {
					retVal.append("Die Rolle " + role.getAsMention() + " hat keine Berechtigungen.");
				}
			} else {
				retVal.append("Den Befehl darf jeder verwenden, weshalb keine zusätzlichen Berechtigungen vergeben wurden.");
			}
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private void removeMember(GuildMessageChannel channel, String cmdName, Member member) {
		if(hasCommand(channel, cmdName)) {
			StringBuilder retVal = new StringBuilder();
			GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
			if(guildData.removeCmdMember(cmdName, member.getIdLong())) {
				retVal.append("Die Berechtigungen von " + member.getAsMention() + " wurden entfernt.");
				Antony.getGuildController().saveGuildData(guildData, channel.getGuild());
			} else {
				retVal.append(member.getAsMention() + " hat keine Berechtigungen.");
			}
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private void clearCommandPermissions(GuildMessageChannel channel, String cmdName) {
		if(hasCommand(channel, cmdName)) {
			StringBuilder retVal = new StringBuilder();
			GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
			if(hasElements(guildData.getCmdMembers(cmdName))) {
				guildData.getCmdMembers(cmdName).clear();
			}
			if(hasElements(guildData.getCmdRoles(cmdName))) {
				guildData.getCmdRoles(cmdName).clear();
			}
			Antony.getGuildController().saveGuildData(guildData, channel.getGuild());
			retVal.append("Alle Berechtigungen für den Befehl \"**" + cmdName + "**\" wurden entfernt.");
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private boolean hasCommand(GuildMessageChannel channel, String cmdName) {
		if(Antony.getCmdMan().hasCommand(cmdName)) {
			return true;
		} else {
			channel.sendMessage("Der Befehl \"" + cmdName + "\" existiert nicht.").queue();
			return false;
		}
	}
	
	private void cleanCmdLists(Guild guild, String cmdName, ArrayList<Long> roles, ArrayList<Long> members) {
		if(!roles.isEmpty() || !members.isEmpty()) {
			GuildData guildData = Antony.getGuildController().loadGuildData(guild);
			for(long role : roles) {
				guildData.removeCmdRole(cmdName, role);
			}
			for(long member : members) {
				guildData.removeCmdMember(cmdName, member);
			}
			Antony.getGuildController().saveGuildData(guildData, guild);
		}
	}
	
	private boolean hasElements(ArrayList<Long> list) {
		if(list != null && !list.isEmpty()) {
			return true;
		}
		return false;
	}
}
