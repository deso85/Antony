package bot.antony.commands;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import bot.antony.antcheck.client.AntCheckClient;
import bot.antony.antcheck.client.dto.Shop;
import bot.antony.antcheck.client.dto.Specie;
import bot.antony.antcheck.client.dto.Variant;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class CallAntcheck implements ServerCommand {

	static Logger logger = LoggerFactory.getLogger(CallAntcheck.class);

	static Map<String, String> CURRENCY_MAP;
	static {
		CURRENCY_MAP = new HashMap<String, String>();
		CURRENCY_MAP.put("at", "€");
		CURRENCY_MAP.put("au", "A$");
		CURRENCY_MAP.put("ch", "CHF");
		CURRENCY_MAP.put("de", "€");
		CURRENCY_MAP.put("gb", "£");
		CURRENCY_MAP.put("th", "£");
		CURRENCY_MAP.put("uk", "£");
	}

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
		client.register(instance);
		instance.registerProvider(ResteasyJackson2Provider.class);
		ResteasyWebTarget target = client.target(AntCheckClient.BASE_URL);
		AntCheckClient antCheckClient = target.proxy(AntCheckClient.class);

		String[] userMessage = message.getContentDisplay().split(" ");

		if (userMessage.length > 1) {
			// get ant names to work with
			String[] antNames = getAntNames(userMessage);

			List<Specie> species = getSpecies(antCheckClient, antNames[1]);

			if (species.isEmpty()) {
				channel.sendMessage(
						"Es konnte keine Ameisenart mit \"" + antNames[0] + "\" im Namen gefunden werden.\r\n"
								+ "Bitte überprüfe die Schreibweise und versuche es erneut.")
						.queue();
			} else {
				List<Specie> speciesWithVariants = species.stream()
						.filter(s -> s.getVariants() != null && s.getVariants() > 0).collect(Collectors.toList());

				// if species exist without any variant
				if (speciesWithVariants.isEmpty()) {
					String outputMessage = null;
					if (species.size() > 1) {
						outputMessage = handleSpeciesFoundWithoutVariants(species);
					} else {
						outputMessage = handleOnlyOneSpecieFoundWithoutVariant(species);
					}
					channel.sendMessage(outputMessage).queue();
				} else {
					if (speciesWithVariants.size() == 1) {
						handleOnlyOneSpecieWithVariant(channel, antCheckClient, speciesWithVariants);
					} else {
						handleMultipleSpeciesWithVariantsFound(channel, speciesWithVariants);
					}

				}
			}

		}

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

	private List<Variant> getVariants(AntCheckClient client, String id) {
		Response response = client.getVariants(id, null);
		String responsePayload = response.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(responsePayload, new TypeReference<List<Variant>>() {
			});
		} catch (JsonProcessingException e) {
			return new ArrayList<>();
		} finally {
			response.close();
		}
	}

	private List<Shop> getShops(AntCheckClient client, String id) {
		Response response = client.getShops(id);
		String responsePayload = response.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(responsePayload, new TypeReference<List<Shop>>() {
			});
		} catch (JsonProcessingException e) {
			return new ArrayList<>();
		} finally {
			response.close();
		}
	}

	private String handleOnlyOneSpecieFoundWithoutVariant(List<Specie> species) {
		StringBuilder sb = new StringBuilder();
		sb.append("Es werden aktuell keine *");
		sb.append(species.get(0).getName());
		sb.append("* verkauft.");
		return sb.toString();
	}

	private String handleSpeciesFoundWithoutVariants(List<Specie> species) {
		StringBuilder sb = new StringBuilder();
		sb.append("Folgende Ameisenarten wurden gefunden, stehen aktuell aber nicht zum Verkauf:");
		for (Specie specie : species) {
			sb.append("\n*- ");
			sb.append(specie.getName());
			sb.append("*");
		}
		return sb.toString();
	}

	private void handleOnlyOneSpecieWithVariant(TextChannel channel, AntCheckClient client,
		List<Specie> speciesWithVariants) {
		Specie specie = speciesWithVariants.get(0);
		List<Variant> variants = this.getVariants(client, specie.getId());
		List<Variant> variantsWithShopIds = variants.stream()
				.filter(v -> v.getShopid() != null && !v.getShopid().equals("-1")).collect(Collectors.toList());
		Set<Shop> allShopsForVariants = new HashSet<>();
		for (Variant variant : variantsWithShopIds) {
			List<Shop> shops = this.getShops(client, variant.getShopid());
			allShopsForVariants.addAll(shops);
		}
		// sort shops by name
		List<Shop> sortedShopsForVariants = allShopsForVariants.stream()
				.sorted(Comparator.comparing(Shop::getName, String.CASE_INSENSITIVE_ORDER))
				.collect(Collectors.toList());
		// build an embedded message
		EmbedBuilder eb = new EmbedBuilder();
		String specieName = specie.getName();
		eb.setTitle("*" + specieName + "*", "https://antwiki.org/wiki/" + specieName.replace(" ", "_")); // title
		eb.setColor(new Color(31, 89, 152)); // color of side stripe
		eb.setDescription("Die folgenden Daten wurden von https://antcheck.de/ bereitgestellt.\n\n"
				+ "***Achtung:*** Die gelisteten Preise beinhalten keine Versandkosten und können je nach Shop unterschiedlich hoch ausfallen.\n\n"
				+ "***Angebote von MyAnts werden auf Wunsch des Shop-Betreibers nicht mehr mit ausgegeben.***");
		String specieImageurl = specie.getImageurl();
		if (!specieImageurl.isEmpty()) {
			eb.setThumbnail(specieImageurl); // image thumbnail
		} else {
			eb.addField("Bild einreichen",
					"Leider gibt es zu dieser Art noch kein passendes Bild. Du kannst helfen und [***hier***](https://antcheck.de/submit-image) ein Bild einreichen. Vielen Dank!",
					true);
		}

		for (Shop shop : sortedShopsForVariants) {
			StringBuilder messagePart = new StringBuilder();
			List<Variant> variantsForShop = variantsWithShopIds.stream().filter(v -> v.getShopid().equals(shop.getId()))
					.collect(Collectors.toList());

			for (Variant variant : variantsForShop) {
				messagePart.append(variant.getName());
				messagePart.append(": [**" + String.format("%.2f", Double.parseDouble(variant.getPrice())) + " "
						+ CURRENCY_MAP.get(shop.getCountry()) + "**]");
				messagePart.append("(" + variant.getUrl() + ")\n");
				
			}
			// Discord Flag for UK can be shown with :flag_gb: instead of :flag_uk:
			eb.addField(":flag_" + shop.getCountry().replace("uk", "gb") + ": " + shop.getName(), messagePart.toString(), false);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		Date now = new Date();
		StringBuilder sb = new StringBuilder("Preise und Verfügbarkeiten zuletzt aktualisiert: ");
		sb.append(sdf.format(now.getTime()));
		sb.append(" 00:00 Uhr.");
		eb.setFooter(sb.toString());
		channel.sendMessage(eb.build()).queue();
	}

	private void handleMultipleSpeciesWithVariantsFound(TextChannel channel, List<Specie> speciesWithVariants) {
		StringBuilder sb = new StringBuilder("Folgende Ameisenarten wurden im Verkauf gefunden:\n\n");
		for (Specie specie : speciesWithVariants) {
			sb.append("*- ");
			sb.append(specie.getName());
			sb.append("*\n");
		}
		sb.append("\nBitte schränke die Suche weiter ein.");
		channel.sendMessage(sb.toString()).queue();
	}

	/**
	 * Function to return formatted ant names
	 * 
	 * @param args the arguments which came with the read message
	 * @return String[] which consists of 2 strings. The first one with ant species
	 *         name divided by spaces. The second one with ant species name divided
	 *         by underscores.
	 */
	private String[] getAntNames(String[] args) {

		String clearAntName = "";
		String urlAntName = "";

		for (int i = 1; i < args.length; i++) {
			// args[i] = args[i].replace("-", " ");
			clearAntName += (i > 1) ? " " + args[i].replace("-", " ") : args[i].replace("-", " ");
			urlAntName += (i > 1) ? "_" + args[i].replace("-", "_") : args[i].replace("-", "_");
		}
		String[] antNames = { clearAntName, urlAntName };

		return antNames;
	}

}
