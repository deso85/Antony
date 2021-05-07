package bot.antony.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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

import bot.antony.Antony;
import bot.antony.commands.antcheck.client.AntCheckClient;
import bot.antony.commands.antcheck.client.dto.Shop;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.commands.antcheck.client.dto.Variant;
import bot.antony.commands.types.ServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Sells implements ServerCommand {

	static Logger logger = LoggerFactory.getLogger(Sells.class);

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
			// get ant species name to work with
			String antSpeciesName = Utils.getAntSpeciesName(Arrays.copyOfRange(userMessage, 1, userMessage.length));

			List<Specie> species = getSpecies(antCheckClient, antSpeciesName.replace(" ", "_"));

			if (species.isEmpty()) {
				channel.sendMessage(
						"Es konnte keine Ameisenart mit \"" + antSpeciesName + "\" im Namen gefunden werden.\r\n"
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
					// Message may only contain 2000 chars
					if (outputMessage.length() <= 2000) {
						channel.sendMessage(outputMessage).queue();
					} else {
						channel.sendMessage("Es wurden " + species.size() + " Arten mit dem Suchbegriff \""
								+ antSpeciesName + "\" gefunden.\nBitte schränke die Suche weiter ein.").queue();
					}

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
		//Old output - Changerequest to not post variants if they are not on sale
		/*sb.append("Folgende Ameisenarten wurden gefunden, stehen aktuell aber nicht zum Verkauf:");
		for (Specie specie : species) {
			sb.append("\n*- ");
			sb.append(specie.getName());
			sb.append("*");
		}*/
		sb.append("Es wurden " + species.size() + " Ameisenarten gefunden, aber leider werden davon aktuell keine verkauft.");
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
		eb.setTitle("*" + specieName + "*", "https://antwiki.org/wiki/" + specieName.replace(" ", "_"));
		eb.setColor(Antony.getBaseColor());
		eb.setDescription("Die folgenden Daten wurden von https://antcheck.info/ bereitgestellt.\n\n"
				+ "***Achtung:*** Die gelisteten Preise beinhalten keine Versandkosten und können je nach Shop unterschiedlich hoch ausfallen.");
		String specieImageurl = specie.getImageurl();
		if (specieImageurl != null && !specieImageurl.isEmpty()) {
			eb.setThumbnail(specieImageurl);
		} else {
			eb.addField("Bild einreichen",
					"Leider gibt es zu dieser Art noch kein passendes Bild. Du kannst helfen und [***hier***](https://antcheck.info/submit-image) ein Bild einreichen. Vielen Dank!",
					true);
		}

		for (Shop shop : sortedShopsForVariants) {
			StringBuilder messagePart = new StringBuilder();
			List<Variant> variantsForShop = variantsWithShopIds.stream().filter(v -> v.getShopid().equals(shop.getId()))
					.collect(Collectors.toList());

			for (Variant variant : variantsForShop) {
				messagePart.append(variant.getName());
				messagePart.append(": [**" + String.format("%.2f", Double.parseDouble(variant.getPrice())) + " "
						+ shop.getCurrency() + "**]");
				messagePart.append("(" + variant.getUrl() + ")\n");

			}
			// TODO Check if messagePart is more than 1024 chars (many variants offered) ->
			// Count and add another field if needed
			eb.addField(":flag_" + shop.getCountry() + ": " + shop.getName(), messagePart.toString(), false);
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

}
