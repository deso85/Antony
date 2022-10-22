package bot.antony.events.reaction.add;

import java.util.ArrayList;
import java.util.Arrays;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class EggReaction extends MessageReaction {


	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public EggReaction() {
		super();
		this.description = "Diese Reaction kann dazu verwendet werden, neuen Benutzern die Ei-Rolle zu geben und damit freizuschalten.";
		this.shortDescription = "Reaction für die Freischaltung neuer User.";
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void perform(MessageReactionAddEvent event) {
		setVariables(event);
		if(shallTrigger(event.getMember())) {
			removeReaction();
			activateUser();
			sendWelcomeMessage();
		}
	}
	
	@Override
	public boolean shallTrigger(Member member) {
		if(super.shallTrigger(member)
				&& Antony.getGuildController().getWelcomeChannel(guild) == message.getChannel()
				&& message.getMember().getRoles().size() == 0) {
			return true;
		}
		return false;
	}
	
	public void activateUser() {
		Role role = guild.getRolesByName("Ei", false).get(0);
		guild.addRoleToMember(message.getMember(), role).complete();
	}
	
	public void sendWelcomeMessage() {
		responseChannel.sendMessage(getRandomWelcomeText()).queue();
	}

	public String getRandomWelcomeText() {
		TextChannel roles = Antony.getGuildController().getValidChannel(guild, Arrays.asList(745657357962313749L, 778960515895918628L));
		TextChannel serverQuestions = Antony.getGuildController().getValidChannel(guild, Arrays.asList(692317004727844934L, 778960515895918630L));
		TextChannel serverRules = Antony.getGuildController().getValidChannel(guild, Arrays.asList(789417087382192148L, 778960515895918626L));
		TextChannel memberIntroduction = Antony.getGuildController().getValidChannel(guild, Arrays.asList(543512466739691520L, 778960515895918631L));
		
		ArrayList<String> messages = new ArrayList<String>();
		
		messages.add("Willkommen " + message.getMember().getAsMention() + "!\n"
				+ "Du bist nun freigeschaltet und siehst schon mehr Kanäle. "
				+ "Falls du noch weitere tolle Bereiche z.B. über andere Tiere und Pflanzen oder die Haltungsberichte entdecken möchtest, sieh dir in der " + roles.getAsMention() + " die Module an! "
				+ "Bei Fragen melde dich gerne bei einem Mod oder in " + serverQuestions.getAsMention() + ".\nViel Spaß!");

		messages.add("Willkommen " + message.getMember().getAsMention() + "!\n"
				+ "Du bist nun freigeschaltet und kannst auch in anderen Bereichen schreiben und dich aktiv einbringen. "
				+ "Schau dir gerne noch den Kanal " + serverRules.getAsMention() + " an, bevor du richtig los legst. "
				+ "Weitere Server-Inhalte kannst du in dem Kanal " + roles.getAsMention() + " freischalten. "
				+ "Bei Fragen kannst du dich jeder Zeit an einen Mod wenden oder dich in " + serverQuestions.getAsMention() + " melden."
				+ "\nUnd nun viel Spaß bei uns!");
		
		messages.add("Willkommen " + message.getMember().getAsMention() + "!\n"
				+ "Du bist nun freigeschaltet und siehst schon mehr Kanäle. "
				+ "Weitere Themen, die dich interessieren könnten, kannst du in " + roles.getAsMention() + " als Module zuschalten! "
				+ "Nützliche Informationen findest du in " + serverRules.getAsMention() + ". "
				+ "In " + memberIntroduction.getAsMention() + " kannst du dich, wenn du möchtest, gerne kurz vorstellen."
				+ "\nViel Spaß auf AAM!");
		
		return messages.get((int) (System.currentTimeMillis() % messages.size()));
	}
	
}
