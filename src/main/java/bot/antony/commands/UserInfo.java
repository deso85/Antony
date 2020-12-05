package bot.antony.commands;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class UserInfo implements ServerCommand {

	List<Member> memberList;
	Member member;
	String memberOnlineStatus;

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		setMemberList(new ArrayList<>());
		
		String[] userMessage = message.getContentDisplay().split(" ");

		// fill memberList
		for (Member member : channel.getGuild().getMembers()) {
			if (!getMemberList().contains(member)) {
				getMemberList().add(member);
			}
		}

		// sort memberList by joined Time
		Collections.sort(getMemberList(), (member1, member2) -> {
			return member1.getTimeJoined().compareTo(member2.getTimeJoined());
		});

		// overwrite member
		if (userMessage.length > 1) {			
			if(message.getMentionedMembers().size() > 0) {
				setMember(message.getMentionedMembers().get(0));
			} else {
				setMember(findUserIn(channel, userMessage[1]));
			}
		} else {
			setMember(m);
		}

		if (getMember() != null) {

			// TODO Search for guild emotes to use
			if (getMember().getOnlineStatus().equals(OnlineStatus.ONLINE)) {
				setUserOnlineStatus("🟢"); // :green_circle:
			} else if (getMember().getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
				setUserOnlineStatus("⚫"); // :black_circle:
			} else if (getMember().getOnlineStatus().equals(OnlineStatus.IDLE)) {
				setUserOnlineStatus("🌙"); // :crescent_moon:
			} else if (getMember().getOnlineStatus().equals(OnlineStatus.DO_NOT_DISTURB)) {
				setUserOnlineStatus("⛔"); // :no_entry:
			} else {
				setUserOnlineStatus("⚪"); // :white_circle:
			}


			channel.sendMessage(getUserEB().build()).queue();

		} else {
			channel.sendMessage("Ich konnte niemanden mit dem Namen \"" + userMessage[1] + "\" finden.").queue();
		}

	}

	private EmbedBuilder getUserEB() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

		//Build Title or Author
		StringBuilder ebHeadline = new StringBuilder();
		ebHeadline.append(getUserOnlineStatus() + " ");
		ebHeadline.append(getMember().getUser().getAsTag());
		if (getMember().getNickname() != null) {
			ebHeadline.append(" - " + getMember().getNickname());
		}
		
		//Build Roles String
		StringBuilder sbRoles = new StringBuilder();
		if(getMember().getRoles().size() > 0) {
			Iterator<Role> roleIterator = getMember().getRoles().iterator();
			while(roleIterator.hasNext()) {
				sbRoles.append(roleIterator.next().getAsMention());
				if(roleIterator.hasNext()) {
					sbRoles.append(", ");
				}
			}
			
		} else {
			sbRoles.append("-");
		}
		
		//Build status string
		StringBuilder sbStatus = new StringBuilder();
		if(!getMember().getActivities().isEmpty()) {
			sbStatus.append("Status: ");
			if(getMember().getActivities().get(0).getEmoji() != null) {
				sbStatus.append(getMember().getActivities().get(0).getEmoji().getAsMention());
			}
			sbStatus.append(" " + getMember().getActivities().get(0).getName());
		} else {
			sbStatus.append("Ist aktuell im " + getMember().getOnlineStatus().getKey() + " Status");
		}
		
		//Build Nicknames
		// TODO read saved nicknames the user had
		
		EmbedBuilder eb = new EmbedBuilder()
				.setAuthor(ebHeadline.toString())
				.setDescription(sbStatus.toString())
				.setColor(getMember().getColor())
				.setThumbnail(getMember().getUser().getEffectiveAvatarUrl())
				.addField("Discord beigetreten", getMember().getTimeCreated().format(formatter) + "\n(Vor "
						+ getFormattedPeriod(getMember().getTimeCreated(), formatter) + ")",
						false)
				.addField("Server beigetreten", getMember().getTimeJoined().format(formatter) + "\n(Vor "
						+ getFormattedPeriod(getMember().getTimeJoined(), formatter) + ")",
						false)
				/*.addField("Discord beigetreten", getMember().getTimeCreated().format(formatter) + "\n(Vor "
						+ ChronoUnit.DAYS.between(getMember().getTimeCreated(), OffsetDateTime.now()) + " Tagen)", true)
				.addField("Server beigetreten", getMember().getTimeJoined().format(formatter) + "\n(Vor "
						+ ChronoUnit.DAYS.between(getMember().getTimeJoined(), OffsetDateTime.now()) + " Tagen)", true)*/
				.addField("Rollen", sbRoles.toString(), false)
				.setFooter("Member #" + (getMemberList().indexOf(getMember())+1) + " | User ID: " + getMember().getId());
		
		return eb;

	}
	
	private String getFormattedPeriod(OffsetDateTime odt, DateTimeFormatter formatter) {
		StringBuilder sb = new StringBuilder();
		LocalDate currentDate = LocalDate.now();
		LocalDate passedDate = LocalDate.parse(odt.format(formatter), formatter);
		Period period = Period.between(passedDate, currentDate);
		//Years
		if(period.getYears() > 0) {
			sb.append(period.getYears() + " Jahr");
			if(period.getYears() > 1) {
				sb.append("en");
			}
			sb.append(", ");
		}
		//Months
		if(period.getYears() > 0 || period.getMonths() > 0) {
			sb.append(period.getMonths() + " Monat");
			if(period.getMonths() != 1) {
				sb.append("en");
			}
			sb.append(", ");
		}
		//Days
		sb.append(period.getDays() + " Tag");
		if(period.getDays() != 1) {
			sb.append("en");
		}
		
		return sb.toString();
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
		// List<Member> users = channel.getGuild().getMembers();
		List<Member> users = memberList;
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
	public List<Member> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<Member> memberList) {
		this.memberList = memberList;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public String getUserOnlineStatus() {
		return memberOnlineStatus;
	}

	public void setUserOnlineStatus(String userOnlineStatus) {
		this.memberOnlineStatus = userOnlineStatus;
	}
}
