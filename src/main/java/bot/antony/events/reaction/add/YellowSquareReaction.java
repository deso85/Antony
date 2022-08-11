package bot.antony.events.reaction.add;

import java.util.List;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class YellowSquareReaction extends MessageReaction {

	protected String roleName = "GELBE KARTE :(";
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public YellowSquareReaction() {
		super();
		this.description = "Diese Reaction kann dazu verwendet werden, Benutzern die Rolle \"GELBE KARTE :(\" hinzuzufügen oder zu entfernen.";
		this.shortDescription = "Gibt oder entfernt einem User die Rolle \"GELBE KARTE :(\".";
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void perform(MessageReactionAddEvent event) {
		setVariables(event);
		if(shallTrigger(event.getMember())) {
			removeReaction();
			mentionReactor();
			printInfo(toggleYellowCard());
		}
	}
	
	@Override
	public boolean shallTrigger(Member member) {
		if(super.shallTrigger(member)
				&& findGuildRole() != null) {
			return true;
		}
		return false;
	}
	
	private boolean toggleYellowCard() {
		Role role = findGuildRole();
		if(findMemberRole() != null) {
			//remove role
			guild.removeRoleFromMember(message.getMember(), role).complete();
			return false;
		} else {
			//set role
			guild.addRoleToMember(message.getMember(), role).complete();
			return true;
		}
	}
	
	private void printInfo(boolean roleAdded) {
		StringBuilder sb = new StringBuilder();
		sb.append("ID: " + message.getAuthor().getId() + "\n");
		sb.append("Tag: " + message.getAuthor().getAsTag() + "\n");
		sb.append("Name: " + message.getAuthor().getName());
		if(message.getMember().getNickname() != null) {
			sb.append("\nNickname: " + message.getMember().getNickname());
		}
		if(responseChannel != null) {
			if(roleAdded) {
				responseChannel.sendMessage("__Gelbe Karte (GK) vergeben an:__").complete();
			} else {
				responseChannel.sendMessage("__Gelbe Karte (GK) entfernt von:__").complete();
			}
			responseChannel.sendMessage(sb.toString()).queue();
		}
	}
	
	private Role findGuildRole() {
	    List<Role> roles = guild.getRoles();
	    return roles.stream()
	                .filter(role -> role.getName().equals(roleName)) // filter by role name
	                .findFirst() // take first result
	                .orElse(null); // else return null
	}
	
	public Role findMemberRole() {
	    List<Role> roles = message.getMember().getRoles();
	    return roles.stream()
	                .filter(role -> role.getName().equals(roleName)) // filter by role name
	                .findFirst() // take first result
	                .orElse(null); // else return null
	}
	
	@Override
	public void setVariables(MessageReactionAddEvent event) {
		super.setVariables(event);
		responseChannel = Antony.getGuildController().getLogChannel(event.getGuild());
	}
}
