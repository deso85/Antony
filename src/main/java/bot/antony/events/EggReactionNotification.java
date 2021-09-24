package bot.antony.events;

import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EggReactionNotification extends ListenerAdapter {

	Guild guild;
	Message message;
	Member reactor;
	Member member;
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		guild = event.getGuild();
		
		if(event.getReactionEmote().getName().equals("🥚")) {	//egg emoji :egg:
			
			Long welcomeChannelID;
			if(Antony.isProdStage()) {
				welcomeChannelID = 554261873483055114L;
			} else {
				welcomeChannelID = 778960515895918627L;
			}
			
			//Is this the welcome channel? Shall not work in other channels
			if(event.getChannel().getIdLong() == welcomeChannelID) {
				message = event.retrieveMessage().complete();
				reactor = guild.getMember(event.getUser());
				member = message.getMember();
				List<String> allowedRoles = new ArrayList<String>();
				
				//Roles which may use the command
				allowedRoles.add("Admin");
				allowedRoles.add("Soldat");

				if(Utils.memberHasRole(reactor, allowedRoles) && member.getRoles().size() == 0) {
					message.removeReaction(event.getReactionEmote().getName(), event.getUser()).queue();
					Role role = guild.getRolesByName("Ei", false).get(0);
					guild.addRoleToMember(member, role).complete();
					event.getChannel().sendMessage(getRandomWelcomeText()).queue();
				}
			}
		}
	}
	
	public Role findRole(Member member, String name) {
	    List<Role> roles = member.getRoles();
	    return roles.stream()
	                .filter(role -> role.getName().equals(name)) // filter by role name
	                .findFirst() // take first result
	                .orElse(null); // else return null
	}
	
	public String getRandomWelcomeText() {
		TextChannel roles;
		TextChannel serverQuestions;
		TextChannel serverRules;
		TextChannel memberIntroduction;
		if(Antony.isProdStage()) {
			roles = guild.getTextChannelById(745657357962313749L);
			serverQuestions = guild.getTextChannelById(692317004727844934L);
			serverRules = guild.getTextChannelById(789417087382192148L);
			memberIntroduction = guild.getTextChannelById(543512466739691520L);
		} else {
			roles = guild.getTextChannelById(778960515895918628L);
			serverQuestions = guild.getTextChannelById(778960515895918630L);
			serverRules = guild.getTextChannelById(778960515895918626L);
			memberIntroduction = guild.getTextChannelById(778960515895918631L);
		}
		ArrayList<String> messages = new ArrayList<String>();
		
		messages.add("Willkommen " + member.getAsMention() + "!\n"
				+ "Du bist nun freigeschaltet und siehst schon mehr Kanäle. "
				+ "Falls du noch weitere tolle Bereiche z.B. über andere Tiere und Pflanzen oder die Haltungsberichte entdecken möchtest, sieh dir in der " + roles.getAsMention() + " die Module an! "
				+ "Bei Fragen melde dich gerne bei einem Mod oder in " + serverQuestions.getAsMention() + ".\nViel Spaß!");

		messages.add("Willkommen " + member.getAsMention() + "!\n"
				+ "Du bist nun freigeschaltet und kannst auch in anderen Bereichen schreiben und dich aktiv einbringen. "
				+ "Schau dir gerne noch den Kanal " + serverRules.getAsMention() + " an, bevor du richtig los legst. "
				+ "Weitere Server-Inhalte kannst du in dem Kanal " + roles.getAsMention() + " freischalten. "
				+ "Bei Fragen kannst du dich jeder Zeit an einen Mod wenden oder dich in " + serverQuestions.getAsMention() + " melden."
				+ "\nUnd nun viel Spaß bei uns!");
		
		messages.add("Willkommen " + member.getAsMention() + "!\n"
				+ "Du bist nun freigeschaltet und siehst schon mehr Kanäle. "
				+ "Weitere Themen, die dich interessieren könnten, kannst du in " + roles.getAsMention() + " als Module zuschalten! "
				+ "Nützliche Informationen findest du in " + serverRules.getAsMention() + ". "
				+ "In " + memberIntroduction.getAsMention() + " kannst du dich, wenn du möchtest, gerne kurz vorstellen."
				+ "\nViel Spaß auf AAM!");
		
		return messages.get((int) (System.currentTimeMillis() % messages.size()));
	}
}
