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

	private List<Member> memberList;
	private Member member;
	private String memberOnlineStatus;
	private String fullMemberName;
	

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		setMemberList(new ArrayList<>());
		String[] userMessage = message.getContentDisplay().split(" ");
		boolean outputlight = false;

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

		// initially set the member who called the function
		setMember(m);
		
		// overwrite member if needed
		if (userMessage.length > 1) {
			// user has been mentioned
			if(message.getMentionedMembers().size() > 0) {
				setMember(message.getMentionedMembers().get(0));
			//not a mentioned user so it can be a user or a parameter
			} else {
				// we build a new string to find the user even if we don't know if there is a parameter inside the provided text
				StringBuilder usrMessage = new StringBuilder();
				int messageParts = 0;
				
				// because the command can be used with parameters we have to search for them
				for(String msgpart: userMessage) {
					// if parameter "light" has been provided to have basic user information
					if(msgpart.equals("light")) {
						outputlight = true;
					// part of the command was not a known parameter
					} else {
						messageParts++;
						usrMessage.append(msgpart + " ");
					}
				}
				// if output wasn't slimmed or there are even more parameters we have to search for someone
				if(messageParts > 1) {
					// full member name regarding to provided strings has to be a substring. cut off command and the last space which has been added inside the for loop
					setFullMemberName(usrMessage.substring(userMessage[0].length() + 1, usrMessage.length() - 1));
					setMember(findUserIn(channel, getFullMemberName()));
				}
			}
		}

		if (getMember() != null) {

			// TODO Search for guild emotes to use
			if (getMember().getOnlineStatus().equals(OnlineStatus.ONLINE)) {
				setMemberOnlineStatus("🟢"); // :green_circle:
			} else if (getMember().getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
				setMemberOnlineStatus("⚫"); // :black_circle:
			} else if (getMember().getOnlineStatus().equals(OnlineStatus.IDLE)) {
				setMemberOnlineStatus("🌙"); // :crescent_moon:
			} else if (getMember().getOnlineStatus().equals(OnlineStatus.DO_NOT_DISTURB)) {
				setMemberOnlineStatus("⛔"); // :no_entry:
			} else {
				setMemberOnlineStatus("⚪"); // :white_circle:
			}

			
			if(outputlight) {
				StringBuilder sb = new StringBuilder();
				
				sb.append("ID: " + getMember().getId() + "\n");
				sb.append("Tag: " + getMember().getUser().getAsTag() + "\n");
				sb.append("Nick: " + getMember().getUser().getName() + "\n");
				
				channel.sendMessage(sb.toString()).queue();
			} else {
				channel.sendMessage(getUserEB().build()).queue();
			}
			

		} else {
			channel.sendMessage("Ich konnte niemanden mit dem Namen " + getFullMemberName() + " finden.").queue();
		}

	}

	private EmbedBuilder getUserEB() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

		//Build Title or Author
		StringBuilder ebHeadline = new StringBuilder();
		ebHeadline.append(getMemberOnlineStatus() + " ");
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
		List<Member> users = getMemberList();
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

	public String getMemberOnlineStatus() {
		return memberOnlineStatus;
	}

	public void setMemberOnlineStatus(String userOnlineStatus) {
		this.memberOnlineStatus = userOnlineStatus;
	}
	
	public String getFullMemberName() {
		return fullMemberName;
	}

	public void setFullMemberName(String fullMemberName) {
		this.fullMemberName = fullMemberName;
	}
}