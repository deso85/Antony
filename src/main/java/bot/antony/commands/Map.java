package bot.antony.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import bot.antony.Antony;
import bot.antony.commands.antcheck.client.AntCheckClient;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.commands.types.ServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Map implements ServerCommand {

	private TextChannel channel;
	
	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		this.channel = channel;
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
		client.register(instance);
		instance.registerProvider(ResteasyJackson2Provider.class);
		ResteasyWebTarget target = client.target(AntCheckClient.BASE_URL);
		AntCheckClient antCheckClient = target.proxy(AntCheckClient.class);
		
		String[] userMessage = message.getContentDisplay().split(" ");
		
		if (userMessage.length > 1) {
			// get ant species name to work with
			String antSpeciesName = Utils.getAntSpeciesName(Arrays.copyOfRange(userMessage, 1, userMessage.length));
			List<Specie> species = getSpecies(antCheckClient, antSpeciesName.replace(" ", "_"));
			
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
					channel.sendMessage(eb.build()).queue();
				}
			}
			
		} else {
			printHelp();
		}
		
	}
	
	private void printHelp() {
		//TODO: Help ausformulieren
		channel.sendMessage("Benutzung: " + Antony.getCmdPrefix() + "map (Ant Species)\n"
				+ "Beispiel: " + Antony.getCmdPrefix() + "map Lasius niger").queue();
	}
	
	private List<Specie> getSpecies(AntCheckClient client, String antName) {
		Response response = client.getSpecies(antName);
		String responsePayload = response.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(responsePayload, new TypeReference<List<Specie>>() {
			});
		} catch (JsonProcessingException e) {
			return new ArrayList<>();
		} finally {
			response.close();
		}
	}

}