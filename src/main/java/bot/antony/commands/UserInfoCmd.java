package bot.antony.commands;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.controller.UserController;
import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class UserInfoCmd extends ServerCommand {

	private UserController usrCntrl;
	private List<Member> memberList;
	private Member member;
	private String memberOnlineStatus;
	private String fullMemberName;
	private Guild guild;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public UserInfoCmd() {
		super();
		this.privileged = false;
		this.name = "userinfo";
		this.description = "Zeigt Details über einen Benutzer.";
		this.shortDescription = "Zeigt Details über einen Benutzer.";
		this.example = "MemberName";
		this.cmdParams.put("[@Member | MemberName | UserID]", "Zeigt Informationen über den referenzierten Benutzer.");
	}

	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		usrCntrl = Antony.getUserController();
		guild = channel.getGuild();
		setMemberList(guild.getMembers().stream().collect(Collectors.toList()));
		String[] userMessage = message.getContentDisplay().split(" ");
		boolean outputlight = false;
		
		// sort memberList by joined Time
		Collections.sort(getMemberList(), (member1, member2) -> {
			return member1.getTimeJoined().compareTo(member2.getTimeJoined());
		});

		// initially set the member who called the function
		setMember(member);
		
		// overwrite member if needed
		if (userMessage.length > 1) {
			// user has been mentioned
			if(message.getMentions().getMembers().size() > 0) {
				setMember(message.getMentions().getMembers().get(0));
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
					
					if(Utils.isId(fullMemberName)) {
						setMember(guild.getMemberById(Long.parseLong(fullMemberName)));
						//In case the users name is a number
						if (getMember() == null) {
							setMember(findUserIn(channel, getFullMemberName()));
						}
					} else {
						setMember(findUserIn(channel, getFullMemberName()));
					}
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
				sb.append("Name: " + getMember().getUser().getName().replace("|", "\\|"));
				if(getMember().getNickname() != null) {
					sb.append("\nNickname: " + getMember().getNickname().replace("|", "\\|"));
				}
				
				channel.sendMessage(sb.toString()).queue();
			} else {
				channel.sendMessageEmbeds(getUserEB().build()).queue();
			}
			

		} else {
			//User was not found. Send message to channel
			StringBuilder sb = new StringBuilder();
			sb.append("Ich konnte niemanden mit dem Namen");
			if(Utils.isId(fullMemberName)) {
				sb.append("/der ID");
			}
			sb.append(" " + getFullMemberName() + " finden.");
			channel.sendMessage(sb.toString()).queue();
		}

	}

	private EmbedBuilder getUserEB() {
		UserData user = usrCntrl.loadUserData(getMember());
		
		//Set last online
		String lastOnline;
		if(getMember().getOnlineStatus().equals(OnlineStatus.ONLINE) ||
				getMember().getOnlineStatus().equals(OnlineStatus.DO_NOT_DISTURB) ||
				getMember().getOnlineStatus().equals(OnlineStatus.IDLE)) {
			lastOnline = "jetzt";
			user.setLastOnline(System.currentTimeMillis());
		} else {
			if(user.getLastOnline() != null) {
				lastOnline = "<t:" + Instant.ofEpochMilli(user.getLastOnline()).getEpochSecond() + ":R>";
			} else {
				lastOnline = "?";
			}
		}

		//Set names
		StringBuilder names = new StringBuilder();
		int counter = 0;
		List<String> nameList = user.getNames().values().stream().distinct().collect(Collectors.toList());
		if(nameList.size() > 1) {
			for(String name : nameList) {
				counter++;
				names.append(name);
				if(counter < nameList.size()) {
					names.append(", ");
				}
			}
		}
		
		//Set nicknames
		StringBuilder nicknames = new StringBuilder();
		counter = 0;
		List<String> nickList = user.getNicknames().values().stream().distinct().collect(Collectors.toList());
		for(String nick : nickList) {
			if(nick != null) {
				if(counter > 0 && counter < nickList.size()) {
					nicknames.append(", ");
				}
				nicknames.append(nick);
				counter++;
			}
		}
		
		//Save updated user data
		usrCntrl.saveUserData(user, guild);

		//Build Title or Author
		StringBuilder ebHeadline = new StringBuilder();
		ebHeadline.append(getMemberOnlineStatus() + " ");
		ebHeadline.append(getMember().getUser().getAsTag());
		if (getMember().getNickname() != null) {
			ebHeadline.append(" - " + getMember().getNickname());
		}
		
		//Build status string
		StringBuilder sbStatus = new StringBuilder();
		if(!getMember().getActivities().isEmpty()) {
			sbStatus.append("Status: ");
			if(getMember().getActivities().get(0).getEmoji() != null) {
				//TODO: getFormatted() sends plain text and doesn't convert to an Emoji
				sbStatus.append(getMember().getActivities().get(0).getEmoji().getFormatted());
			}
			sbStatus.append(" " + getMember().getActivities().get(0).getName());
		} else {
			sbStatus.append("Ist aktuell im " + getMember().getOnlineStatus().getKey() + " Status");
		}
		
		EmbedBuilder eb = new EmbedBuilder()
				.setAuthor(ebHeadline.toString())
				.setDescription(sbStatus.toString())
				.setColor(getMember().getColor())
				.setThumbnail(getMember().getUser().getEffectiveAvatarUrl())
				.addField("Discord beigetreten", "<t:" + getMember().getTimeCreated().toEpochSecond() + ":f>"
						+ "\n(<t:" + getMember().getTimeCreated().toEpochSecond() + ":R>)",
						true)
				.addField("Server beigetreten", "<t:" + getMember().getTimeJoined().toEpochSecond() + ":f>"
						+ "\n(<t:" + getMember().getTimeJoined().toEpochSecond() + ":R>)",
						true)
				.addField("Zuletzt online gesehen", lastOnline, false);
		
		if(names.length() > 0) {
			eb.addField("Bekannte Namen", names.toString().replace("|", "\\|"), false);
		}
		if(nicknames.length() > 0) {
			eb.addField("Bekannte Nicknames", nicknames.toString().replace("|", "\\|"), false);
		}
				
		eb.setFooter("Member #" + (getMemberList().indexOf(getMember())+1) + " | User ID: " + getMember().getId());
		
		//Build Roles String
		if(getMember().getRoles().size() > 0) {
			StringBuilder sbRoles = new StringBuilder();
			Iterator<Role> roleIterator = getMember().getRoles().iterator();
			while(roleIterator.hasNext()) {
				sbRoles.append(roleIterator.next().getAsMention());
				if(roleIterator.hasNext()) {
					sbRoles.append(", ");
				}
			}
			eb.addField("Rollen", sbRoles.toString(), false);
		}
		
		return eb;
	}
	
	/**
	 * Attempts to find a user in a channel, first look for account name then for
	 * nickname
	 *
	 * @param channel    the channel to look in
	 * @param searchText the name to look for
	 * @return IUser | null
	 */
	public Member findUserIn(GuildMessageChannel channel, String searchText) {
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