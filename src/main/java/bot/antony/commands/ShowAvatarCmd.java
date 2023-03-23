package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;

import bot.antony.commands.types.ServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ShowAvatarCmd extends ServerCommand {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ShowAvatarCmd() {
		super();
		this.privileged = false;
		this.name = "showavatar";
		this.description = "Zeigt eine vergrößerte Version des Avatars/Profilbildes eines Benutzers.";
		this.shortDescription = "Zeigt das Profilbild eines Benutzers.";
		this.example = "Antony";
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		String fullMemberName = "";
		
		// overwrite member
		if (userMessage.length > 1) {
			if(message.getMentions().getMembers().size() > 0) {
				member = message.getMentions().getMembers().get(0);
			} else {
				fullMemberName = message.getContentDisplay().substring(userMessage[0].length()+1);
				
				if(Utils.isId(fullMemberName)) {
					member = channel.getGuild().getMemberById(Long.parseLong(fullMemberName));
					//In case the users name is a number
					if (member == null) {
						member = findUserIn(channel, fullMemberName);
					}
				} else {
					member = findUserIn(channel, fullMemberName);
				}
			}
		}

		if (member != null) {
			channel.sendMessage(member.getEffectiveAvatarUrl() + "?size=2048").queue();
		} else {
			channel.sendMessage("Ich konnte niemanden mit dem Namen " + fullMemberName + " finden.").queue();
		}
	}

	/**
	 * Attempts to find a user in a channel, first look for account name then for
	 * nickname
	 *
	 * @param channel    the channel to look in
	 * @param searchText the name to look for
	 * @return IUser | null
	 */
	public Member findUserIn(TextChannel channel, String searchText) {
		List<Member> users = channel.getGuild().getMembers();
		List<Member> potential = new ArrayList<>();
		int smallestDiffIndex = 0, smallestDiff = -1;
		for (Member u : users) {
			String nick = u.getEffectiveName();
			if (nick.equalsIgnoreCase(searchText)) {
				return u;
			}
			if (nick.toLowerCase().contains(searchText)) {
				potential.add(u);
				int d = Math.abs(nick.length() - searchText.length());
				if (d < smallestDiff || smallestDiff == -1) {
					smallestDiff = d;
					smallestDiffIndex = potential.size() - 1;
				}
			}
		}
		if (!potential.isEmpty()) {
			return potential.get(smallestDiffIndex);
		}
		return null;
	}

}
