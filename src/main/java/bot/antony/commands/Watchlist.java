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
						Antony.getWatchlistController().addString(userMessage[2].toLowerCase());
					}
					break;
					
				case "remove":
					if(userMessage.length > 2) {
						Antony.getWatchlistController().removeString(userMessage[2].toLowerCase());
					}
					break;
					
				case "list":
					List<String> wlist = Antony.getWatchlistController().getWatchlist();
					StringBuilder sb = new StringBuilder();
					if(wlist.size() > 0) {
						sb.append("Bei folgenden wörtern wird gewarnt:\n");
					}
					sb.append(Antony.getWatchlistController().toString());
					channel.sendMessage(sb.toString()).queue();
					break;
					
				case "clear":
					Antony.getWatchlistController().clearList();
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
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "watchlist (add|remove|list|clear) [TextString]").queue();
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