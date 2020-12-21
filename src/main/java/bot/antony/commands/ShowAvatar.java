package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;

import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class ShowAvatar implements ServerCommand {

	private Member member;

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		String fullMemberName = message.getContentDisplay().substring(userMessage[0].length()+1);
		
		// overwrite member
		if (userMessage.length > 1) {			
			if(message.getMentionedMembers().size() > 0) {
				setMember(message.getMentionedMembers().get(0));
			} else {
				setMember(findUserIn(channel, fullMemberName));
			}
		} else {
			setMember(m);
		}

		if (getMember() != null) {

			channel.sendMessage(getMember().getUser().getEffectiveAvatarUrl() + "?size=2048").queue();

		} else {
			channel.sendMessage("Ich konnte niemanden mit dem Namen \"" + fullMemberName + "\" finden.").queue();
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

	// --------------------------------------------------
	// Getters and Setters
	// --------------------------------------------------
	
	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

}
