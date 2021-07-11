package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Watchlist implements ServerCommand {

	private TextChannel channel;

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		List<String> allowedRoles = new ArrayList<String>();
		
		//Roles which may use the command
		allowedRoles.add("Admin");
		allowedRoles.add("Soldat");
		allowedRoles.add("Intermorphe");
		
		
		if(Utils.memberHasRole(member, allowedRoles)) {
			setChannel(channel);
	
			String[] userMessage = message.getContentDisplay().split(" ");
	
			if (userMessage.length > 1) {
	
				switch (userMessage[1].toLowerCase()) {
	
				case "add":
					if(userMessage.length > 2) {
						Antony.getWatchlistController().addWatchlistEntry(userMessage[2].toLowerCase());
					} else {
						printHelp();
					}
					break;
					
				case "remove":
					if(userMessage.length > 2) {
						Antony.getWatchlistController().removeWatchlistEntry(userMessage[2].toLowerCase());
					} else {
						printHelp();
					}
					break;
					
				case "list":
					List<String> watchlist = Antony.getWatchlistController().getWatchlist();
					StringBuilder sb = new StringBuilder();
					if(watchlist.size() > 0) {
						sb.append("Bei folgenden wörtern wird gewarnt:\n");
					}
					sb.append(Antony.getWatchlistController().list());
					getChannel().sendMessage(sb.toString()).queue();
					break;
					
				case "clear":
					Antony.getWatchlistController().clearWatchlist();
					getChannel().sendMessage("Watchlist geleert.").queue();
					break;
					
				case "whitelist":
					if (userMessage.length > 2) {
						switch (userMessage[2].toLowerCase()) {
							case "add":
								if(userMessage.length > 3) {
									Antony.getWatchlistController().addWhitelistEntry(userMessage[3].toLowerCase());
								} else {
									printWhitelistHelp();
								}
								break;
							case "remove":
								if(userMessage.length > 3) {
									Antony.getWatchlistController().removeWhitelistEntry(userMessage[3].toLowerCase());
								} else {
									printHelp();
								}
								break;
							case "list":
								List<String> whitelist = Antony.getWatchlistController().getWhitelist();
								StringBuilder sbuilder = new StringBuilder();
								if(whitelist.size() > 0) {
									sbuilder.append("Bei folgenden wörtern wird nicht gewarnt:\n");
								}
								sbuilder.append(Antony.getWatchlistController().list("whitelist"));
								getChannel().sendMessage(sbuilder.toString()).queue();
								break;
							case "clear":
								Antony.getWatchlistController().clearWhitelist();
								getChannel().sendMessage("Whitelist geleert.").queue();
								break;
							default:
								printWhitelistHelp();
								break;
						}
					}
					break;
				default:
					printHelp();
					break;
				}
			} else {
				printHelp();
			}
		}

	}
	
	private void printHelp() {
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "watchlist (add|remove|list|clear|whitelist) [TextString]").queue();
	}
	
	private void printWhitelistHelp() {
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "watchlist whitelist (add|remove|list|clear) [TextString]").queue();
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------

	public TextChannel getChannel() {
		return channel;
	}

	public void setChannel(TextChannel channel) {
		this.channel = channel;
	}
}