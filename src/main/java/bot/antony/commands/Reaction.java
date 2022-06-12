package bot.antony.commands;

import java.util.ArrayList;
import java.util.Map.Entry;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.events.reaction.add.MessageReaction;
import bot.antony.guild.GuildData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Reaction extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public Reaction() {
		super();
		this.privileged = true;
		this.name = "reaction";
		this.description = "Mit diesem Befehl lassen sich die Berechtigungen für Bot-Befehle basierend auf Reactions ansehen und verwalten.";
		this.shortDescription = "Berechtigungsverwaltung für Bot-Befehle basierend auf Reactions.";
		this.example = "list";
		this.cmdParams.put("list", "Listet alle verfügbaren Reaction-Befehle auf");
		this.cmdParams.put("show reaction", "Zeigt die Berechtigungen eines Reaction-Befehls");
		this.cmdParams.put("addrole reaction @role", "Erteilt einer Rolle die Berechtigung zur Nutzung eines Reaction-Befehls");
		this.cmdParams.put("addmember reaction @member", "Erteilt einem Member die Berechtigung zur Nutzung eines Reaction-Befehls");
		this.cmdParams.put("removerole reaction @role", "Entfernt einer Rolle die Berechtigung zur Nutzung eines Reaction-Befehls");
		this.cmdParams.put("removemember reaction @member", "Entfernt einem Member die Berechtigung zur Nutzung eines Reaction-Befehls");
		this.cmdParams.put("clear reaction", "Entfernt alle Berechtigungen zur Nutzung eines Reaction-Befehls");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		if(mayUse(member)) {
			String[] userMessage = message.getContentDisplay().split(" ");
			if (userMessage.length > 1) {
				switch (userMessage[1].toLowerCase()) {
				case "list":
					list(channel);
					break;
				case "show":
					if (userMessage.length > 2) {
						show(channel, userMessage[2].replace(":", ""));
					} else {
						printHelp(channel);
					}
					break;
				case "addrole":
					if (userMessage.length > 3 && !message.getMentions().getRoles().isEmpty()) {
						addRole(channel, userMessage[2].replace(":", ""), message.getMentions().getRoles().get(0));
					} else {
						printHelp(channel);
					}
					break;
				case "addmember":
					if (userMessage.length > 3 && !message.getMentions().getMembers().isEmpty()) {
						addMember(channel, userMessage[2].replace(":", ""), message.getMentions().getMembers().get(0));
					} else {
						printHelp(channel);
					}
					break;
				case "removerole":
					if (userMessage.length > 3 && !message.getMentions().getRoles().isEmpty()) {
						removeRole(channel, userMessage[2].replace(":", "").toLowerCase(), message.getMentions().getRoles().get(0));
					} else {
						printHelp(channel);
					}
					break;
				case "removemember":
					if (userMessage.length > 3 && !message.getMentions().getMembers().isEmpty()) {
						removeMember(channel, userMessage[2].replace(":", "").toLowerCase(), message.getMentions().getMembers().get(0));
					} else {
						printHelp(channel);
					}
					break;
				case "clear":
					if (userMessage.length > 2) {
						clearReactionPermissions(channel, userMessage[2].replace(":", "").toLowerCase());
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
	}

	public void list(TextChannel channel) {
		StringBuilder retVal = new StringBuilder();
		
		if(Antony.getReactionMan().getReactions().isEmpty()) {
			retVal.append("Es wurden keine Reaction-Befehle registriert.");
		} else {
			retVal.append("Folgende Reaction-Befehle sind verfügbar:\n");
			for(Entry<String, MessageReaction> entry : Antony.getReactionMan().getReactions().entrySet()) {
				if(channel.getGuild().getEmotesByName(entry.getKey(), false).isEmpty()) {
					retVal.append(entry.getKey());
				} else {
					retVal.append(channel.getGuild().getEmotesByName(entry.getKey(), false).get(0).getAsMention());
				}
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
	
	public void show(TextChannel channel, String reactionName) {
		if(hasReaction(channel, reactionName)) {
			StringBuilder retVal = new StringBuilder();
			if(channel.getGuild().getEmotesByName(reactionName, false).isEmpty()) {
				retVal.append(reactionName);
			} else {
				retVal.append(channel.getGuild().getEmotesByName(reactionName, false).get(0).getAsMention());
			}
			retVal.append(" - Berechtigungen:");
			if(!Antony.getReactionMan().getReaction(reactionName).isPrivileged()) {
				retVal.append("\nJeder kann den Reaction-Befehl ausführen.");
			} else {
				GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
				ArrayList<Long> rolesToDelete = new ArrayList<Long>(); //if isn't available anymore
				ArrayList<Long> membersToDelete = new ArrayList<Long>(); //if isn't available anymore
				
				if(!hasElements(guildData.getReactionRoles(reactionName)) && !hasElements(guildData.getReactionMembers(reactionName))) {
					retVal.append("\nNur der Server-Owner und Server-Administratoren können den Reaction-Befehl ausführen.");
				}
				if(hasElements(guildData.getReactionRoles(reactionName))) {
					retVal.append("\nBerechtigte Rollen: ");
					int roleCount = 1;
					for(Long roleId : guildData.getReactionRoles(reactionName)) {
						if(channel.getGuild().getRoleById(roleId) != null) {
							retVal.append(channel.getGuild().getRoleById(roleId).getAsMention());
							if(roleCount < guildData.getReactionRoles(reactionName).size()) {
								retVal.append(", ");
								roleCount++;
							}
						} else {
							rolesToDelete.add(roleId);
						}
					}
				}
				if(hasElements(guildData.getReactionMembers(reactionName))) {
					retVal.append("\nBerechtigte Member: ");
					int memberCount = 1;
					for(Long memberId : guildData.getReactionMembers(reactionName)) {
						if(channel.getGuild().getMemberById(memberId) != null) {
							retVal.append(channel.getGuild().getMemberById(memberId).getAsMention());
							if(memberCount < guildData.getReactionMembers(reactionName).size()) {
								retVal.append(", ");
								memberCount++;
							}
						} else {
							membersToDelete.add(memberId);
						}
					}
				}
				cleanReactionLists(channel.getGuild(), reactionName, rolesToDelete, membersToDelete);
			}
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private void addRole(TextChannel channel, String reactionName, Role role) {
		if(hasReaction(channel, reactionName)) {
			StringBuilder retVal = new StringBuilder();
			if(Antony.getReactionMan().getReaction(reactionName).isPrivileged()) {
				GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
				if(guildData.addReactionRole(reactionName, role.getIdLong())) {
					retVal.append("Die Rolle " + role.getAsMention() + " darf nun den Reaction-Befehl verwenden.");
					Antony.getGuildController().saveGuildData(guildData, channel.getGuild());
				} else {
					retVal.append("Die Rolle " + role.getAsMention() + " hatte bereits die Berechtigung, um den Reaction-Befehl zu verwenden.");
				}
			} else {
				retVal.append("Den Reaction-Befehl darf jeder verwenden, weshalb keine zusätzlichen Berechtigungen vergeben wurden.");
			}
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private void addMember(TextChannel channel, String reactionName, Member member) {
		if(hasReaction(channel, reactionName)) {
			StringBuilder retVal = new StringBuilder();
			if(Antony.getReactionMan().getReaction(reactionName).isPrivileged()) {
				GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
				if(guildData.addReactionMember(reactionName, member.getIdLong())) {
					retVal.append(member.getAsMention() + " darf nun den Reaction-Befehl verwenden.");
					Antony.getGuildController().saveGuildData(guildData, channel.getGuild());
				} else {
					retVal.append(member.getAsMention() + " hatte bereits die Berechtigung, um den Reaction-Befehl zu verwenden.");
				}
			} else {
				retVal.append("Den Reaction-Befehl darf jeder verwenden, weshalb keine zusätzlichen Berechtigungen vergeben wurden.");
			}
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private void removeRole(TextChannel channel, String reactionName, Role role) {
		if(hasReaction(channel, reactionName)) {
			StringBuilder retVal = new StringBuilder();
			GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
			if(guildData.removeReactionRole(reactionName, role.getIdLong())) {
				retVal.append("Die Berechtigungen der Rolle " + role.getAsMention() + " wurden entfernt.");
				Antony.getGuildController().saveGuildData(guildData, channel.getGuild());
			} else {
				retVal.append("Die Rolle " + role.getAsMention() + " hat keine Berechtigungen.");
			}
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private void removeMember(TextChannel channel, String reactionName, Member member) {
		if(hasReaction(channel, reactionName)) {
			StringBuilder retVal = new StringBuilder();
			GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
			if(guildData.removeReactionMember(reactionName, member.getIdLong())) {
				retVal.append("Die Berechtigungen von " + member.getAsMention() + " wurden entfernt.");
				Antony.getGuildController().saveGuildData(guildData, channel.getGuild());
			} else {
				retVal.append(member.getAsMention() + " hat keine Berechtigungen.");
			}
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private void clearReactionPermissions(TextChannel channel, String reactionName) {
		if(hasReaction(channel, reactionName)) {
			StringBuilder retVal = new StringBuilder();
			GuildData guildData = Antony.getGuildController().loadGuildData(channel.getGuild());
			if(hasElements(guildData.getReactionMembers(reactionName))) {
				guildData.getReactionMembers(reactionName).clear();
			}
			if(hasElements(guildData.getReactionRoles(reactionName))) {
				guildData.getReactionRoles(reactionName).clear();
			}
			Antony.getGuildController().saveGuildData(guildData, channel.getGuild());
			retVal.append("Alle Berechtigungen für den Reaction-Befehl ");
			if(channel.getGuild().getEmotesByName(reactionName, false).isEmpty()) {
				retVal.append(reactionName);
			} else {
				retVal.append(channel.getGuild().getEmotesByName(reactionName, false).get(0).getAsMention());
			}
			retVal.append(" wurden entfernt.");
			channel.sendMessage(retVal.toString()).queue();
		}
	}
	
	private boolean hasReaction(TextChannel channel, String reactionName) {
		if(Antony.getReactionMan().hasReaction(reactionName)) {
			return true;
		} else {
			channel.sendMessage("Der Reaction-Befehl " + reactionName + " existiert nicht.").queue();
			return false;
		}
	}
	
	private void cleanReactionLists(Guild guild, String reactionName, ArrayList<Long> roles, ArrayList<Long> members) {
		if(!roles.isEmpty() || !members.isEmpty()) {
			GuildData guildData = Antony.getGuildController().loadGuildData(guild);
			for(long role : roles) {
				guildData.removeReactionRole(reactionName, role);
			}
			for(long member : members) {
				guildData.removeReactionMember(reactionName, member);
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
