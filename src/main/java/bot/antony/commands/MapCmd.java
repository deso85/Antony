package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.antcheck.AntcheckController;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class MapCmd extends ServerCommand {
	
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public MapCmd() {
		super();
		this.privileged = false;
		this.name = "map";
		this.description = "Zeigt eine Karte mit Markierungen, wo die Ameisen-Art auf der Welt vorkommt.";
		this.shortDescription = "Zeigt wo die Ameisen-Art auf der Welt vorkommt.";
		this.example = "Lasius niger";
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		AntcheckController controller = Antony.getAntcheckController();
		String[] userMessage = message.getContentDisplay().split(" ");
		
		if (userMessage.length > 1) {
			List<Specie> species = new ArrayList<Specie>();
			
			if (userMessage.length > 2) { //Genus and specie name are given
				species = controller.findAnt(userMessage[1], userMessage[2]);
			} else if(userMessage.length > 1) { //only genus or specie name is given
				species = controller.findAnt(userMessage[1]);
			}
			
			StringBuilder antSpeciesName = new StringBuilder();
			for(int i=1; i < userMessage.length; i++) {
				antSpeciesName.append(userMessage[i] + " ");
			}
			
			if (species.isEmpty()) {
				channel.sendMessage(
						"Es konnte keine Ameisenart mit \"" + antSpeciesName.toString() + "\" im Namen gefunden werden.\n"
								+ "Bitte überprüfe die Schreibweise und versuche es erneut.")
						.queue();
			} else {
				
				//Too many species found
				if(species.size() > 1) {
					channel.sendMessage("Es wurden " + species.size() + " Ameisenarten gefunden, bitte schränke deine Suche weiter ein.").queue();
				} else { //There is 1 species found
					//Set variables for embed message
					Specie ant = species.get(0);
					String antName = ant.getName();
					String antUrlName = antName.replace(" ", ".");
					String[] antNameParts = antName.split(" ");
					
					//Build Image URL
					StringBuilder imgUrl = new StringBuilder();
					imgUrl.append("https://antmap.coc.tools/images/");
					imgUrl.append(antNameParts[0] + "/");
					for(String namePart: antNameParts) {
						imgUrl.append(namePart + ".");
					}
					imgUrl.append("png");
										
					EmbedBuilder eb = new EmbedBuilder()
							.setColor(Antony.getBaseColor())
							.setTitle("*" + antName + "*", "https://antmaps.org/?mode=species&species=" + antUrlName)
							.setImage(imgUrl.toString())
							.setFooter("Klicke auf den Titel, um zur interaktiven Karte von antmaps.org zu gelangen.\n\nAntony Version " + Antony.getVersion())
					;
					channel.sendMessageEmbeds(eb.build()).queue();
				}
			}
			
		} else {
			printHelp(channel);
		}
	}
	
}