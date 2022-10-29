package bot.antony.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.antony.Antony;
import bot.antony.commands.antcheck.client.AntCheckClient;
import bot.antony.commands.antcheck.client.dto.Shop;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.commands.antcheck.client.dto.Variant;
import bot.antony.commands.types.IServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class Sells implements IServerCommand {

	static Logger logger = LoggerFactory.getLogger(Sells.class);

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		AntCheckClient antCheckClient = Utils.getAntCheckClient();

		String[] userMessage = message.getContentDisplay().split(" ");

		if (userMessage.length > 1) {
			// get ant species name to work with
			String antSpeciesName = Utils.getAntSpeciesName(Arrays.copyOfRange(userMessage, 1, userMessage.length));

			List<Specie> species = antCheckClient.getSpecies(antSpeciesName.replace(" ", "_"));

			// no ants found
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

	private String handleOnlyOneSpecieFoundWithoutVariant(List<Specie> species) {
		StringBuilder sb = new StringBuilder();
		sb.append("Es werden aktuell keine *");
		sb.append(species.get(0).getName());
		sb.append("* verkauft.");
		return sb.toString();
	}

	private String handleSpeciesFoundWithoutVariants(List<Specie> species) {
		StringBuilder sb = new StringBuilder();
		sb.append("Es wurden " + species.size() + " Ameisenarten gefunden, aber leider werden davon aktuell keine verkauft.");
		return sb.toString();
	}

	private void handleOnlyOneSpecieWithVariant(TextChannel channel, AntCheckClient client, List<Specie> speciesWithVariants) {
		Specie specie = speciesWithVariants.get(0);
		List<Variant> variants = client.getVariants(specie.getId(), null);
		List<Variant> variantsWithShopIds = variants.stream()
				.filter(v -> v.getShopid() != null && !v.getShopid().equals("-1")).collect(Collectors.toList());
		Set<Shop> allShopsForVariants = new HashSet<>();
		
		for (Variant variant : variantsWithShopIds) {
			List<Shop> shops = client.getShops(variant.getShopid());
			allShopsForVariants.addAll(shops);
		}
		
		// sort shops by name
		List<Shop> sortedShopsForVariants = allShopsForVariants.stream()
				.sorted(Comparator.comparing(Shop::getName, String.CASE_INSENSITIVE_ORDER))
				.collect(Collectors.toList());

			List<Field> shopFields = new ArrayList<Field>();
			for(Shop shop : sortedShopsForVariants) {
				
				StringBuilder fieldPart = new StringBuilder();
				List<Variant> variantsForShop = variantsWithShopIds.stream().filter(v -> v.getShopid().equals(shop.getId())).collect(Collectors.toList());
	
				String fieldTopic = ":flag_" + shop.getCountry() + ": " + shop.getName();
				for(int i=0; i<variantsForShop.size(); i++) {
					
					if(i==5) {
						fieldTopic = "";
					}
					
					Variant variant = variantsForShop.get(i);
					fieldPart.append(variant.getName());
					fieldPart.append(": [**" + String.format("%.2f", Double.parseDouble(variant.getPrice())) + " "
								+ shop.getCurrency() + "**]");
					fieldPart.append("(" + variant.getUrl() + ")\n");
					
					if(((i+1) % 5 == 0) && (i+1)<variantsForShop.size()) {
						Field shopField = new Field(fieldTopic, fieldPart.toString(), false);
						shopFields.add(shopField);
						fieldPart = new StringBuilder();
					}
				}
				
				Field shopField = new Field(fieldTopic, fieldPart.toString(), false);
				shopFields.add(shopField);
			}
			
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
			}
			
			StringBuilder sb = new StringBuilder("Preise und Verfügbarkeiten werden täglich mehrfach aktualisiert.");
			eb.setFooter(sb.toString());
			
			
			int ebCharCount = 0;
			for(int i=0; i<shopFields.size(); i++) {
				Field shopField = shopFields.get(i);
				int fieldSize = Math.addExact(shopField.getName().length(), shopField.getValue().length());
				
				if((fieldSize + ebCharCount) > 5000) {
					channel.sendMessageEmbeds(eb.build()).complete();
					eb.clearFields();
					ebCharCount = 0;
				}
				
				ebCharCount += fieldSize;
				eb.addField(shopField);
			}
			channel.sendMessageEmbeds(eb.build()).complete();
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
