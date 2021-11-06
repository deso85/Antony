package bot.antony.events.reaction.add;

import java.util.ArrayList;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class EggReaction extends MessageReaction {


	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public EggReaction(MessageReactionAddEvent event) {
		super(event);
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void play() {
		if(shallTrigger()) {
			removeReaction();
			activateUser();
			sendWelcomeMessage();
		}
	}
	
	@Override
	public boolean shallTrigger() {
		if(Antony.getGuildController().memberIsAdmin(reactor)
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
