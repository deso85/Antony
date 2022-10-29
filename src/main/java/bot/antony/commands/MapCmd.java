package bot.antony.commands;

import java.util.Arrays;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.antcheck.client.AntCheckClient;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.commands.types.ServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

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
	public void performCommand(Member member, TextChannel channel, Message message) {
		AntCheckClient antCheckClient = Utils.getAntCheckClient();
		
		String[] userMessage = message.getContentDisplay().split(" ");
		
		if (userMessage.length > 1) {
			// get ant species name to work with
			String antSpeciesName = Utils.getAntSpeciesName(Arrays.copyOfRange(userMessage, 1, userMessage.length));
			List<Specie> species = antCheckClient.getSpecies(antSpeciesName.replace(" ", "_"));
			
			if (species.isEmpty()) {
				channel.sendMessage(
						"Es konnte keine Ameisenart mit \"" + antSpeciesName + "\" im Namen gefunden werden.\n"
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