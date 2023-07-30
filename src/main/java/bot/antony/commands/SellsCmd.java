package bot.antony.commands;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import bot.antony.Antony;
import bot.antony.commands.antcheck.AntcheckController;
import bot.antony.commands.antcheck.client.dto.Offer;
import bot.antony.commands.antcheck.client.dto.Shop;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class SellsCmd extends ServerCommand {
	
	List<Specie> specieList = new ArrayList<Specie>();
	GuildMessageChannel channel;
	AntcheckController controller;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public SellsCmd() {
		super();
		this.privileged = false;
		this.name = "sells";
		this.description = "Listet zu der gesuchten Ameisenart alle Shops und zugehörigen Preise. Die Shops werden nach Namen sortiert ausgegeben. Die Daten werden von https://antcheck.info zur Verfügung gestellt. Vielen Dank hierfür!";
		this.shortDescription = "Listet zu der gesuchten Ameisenart alle Shops und zugehörigen Preise.";
		this.example = "Lasius niger";
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		this.controller = Antony.getAntcheckController();
		this.channel = channel;
		String[] userMessage = message.getContentDisplay().replaceAll("\\s+", " ").trim().split(" ");
		String searchString = "";
		
		/* ================================================================================
		 * 1. Search for the ant
		 * ================================================================================ */
		if (userMessage.length > 2) { //Genus and specie name are given
			specieList = controller.findAnt(userMessage[1], userMessage[2]);
			searchString = userMessage[1] + " " + userMessage[2];
		} else if(userMessage.length > 1) { //only genus or specie name is given
			specieList = controller.findAnt(userMessage[1]);
			searchString = userMessage[1];
		} else {
			printHelp(channel);
			return;
		}
		
		if(specieList.isEmpty()) { //found no ants with the given search parameters
			sendNoAntMessage(searchString);
		} else if(specieList.size() == 1) { //found an ants with the given search parameters
			showOffersForAnt(specieList.get(0));
		} else { //found multiple ants with the given search parameters
			showMultipleSpeciesWithOffers(searchString);
		}
	}

	private void showMultipleSpeciesWithOffers(String searchString) {
		List<Specie> speciesWithOffers = new ArrayList<Specie>();
		StringBuilder returnString = new StringBuilder();
		
		for(Specie specie : specieList) {
			//Check all species for available offers
			if(!controller.getOffersForAntWithoutBlShops(specie).isEmpty()) {
				speciesWithOffers.add(specie);
			}
		}
		
		if(speciesWithOffers.size() > 1) { // There are multiple species with offers
			returnString.append("Folgende " + speciesWithOffers.size() + " Ameisenarten wurden im Verkauf gefunden:\n\n");
			for(Specie specie : speciesWithOffers.stream()
					.sorted(Comparator.comparing(Specie::getName, String.CASE_INSENSITIVE_ORDER))
	                .collect(Collectors.toList())) {
				if((returnString.length() + specie.getName().length() + 6) >= 2000) {
					channel.sendMessage(returnString.toString()).queue();
					returnString = new StringBuilder();
				}
				returnString.append("*• ");
				returnString.append(specie.getName());
				returnString.append("*\n");
			}
			returnString.append("\nBitte schränke die Suche weiter ein.");
			channel.sendMessage(returnString.toString()).queue();
		} else if(speciesWithOffers.size() == 1) { // There is just 1 species with offers
			channel.sendMessage("Mit der Suche nach \"" + searchString + "\" wurden " + specieList.size() + " Ameisenarten gefunden, gibt es aber nur für ***" + speciesWithOffers.get(0).getName() + "*** Angebote.").queue();
			showOffersForAnt(speciesWithOffers.get(0));
		} else { // No offer for any of the found species
			channel.sendMessage("Mit der Suche nach \"" + searchString + "\" wurden " + specieList.size() + " Ameisenarten gefunden, aber leider werden davon aktuell keine verkauft.").queue();
		}
	}
	
	private void sendNoAntMessage(String searchString) {
		StringBuilder returnString = new StringBuilder();
		
		returnString.append("Es konnte keine Ameisenart mit ");
		if(searchString.contains(" ")) {
			returnString.append("den Suchbegriffen ");
		} else {
			returnString.append("dem Suchbegriff ");
		}
		returnString.append("\"" + searchString + "\" gefunden werden.\n");
		returnString.append("Bitte überprüfe die Schreibweise und versuche es erneut.");
		
		channel.sendMessage(returnString.toString()).queue();
	}
	
	private void showOffersForAnt(Specie ant) {
		List<Offer> offerList = new ArrayList<Offer>();
		List<Shop> shopList = new ArrayList<Shop>();
		// 2. Search for offers with the given ant
		offerList = controller.getOffersForAntWithoutBlShops(ant);
		if(offerList.isEmpty()) {
			channel.sendMessage("Es werden aktuell keine *" + ant.getName() + "* verkauft.").queue();
		} else {
			//3. Get related shops
			shopList = controller.getShopsByOffers(offerList).stream()
					.sorted(Comparator.comparing(Shop::getName, String.CASE_INSENSITIVE_ORDER))
					.collect(Collectors.toList());
			
			/* ================================================================================
			 * 4. Combine shops with offers in embed fields
			 * ================================================================================ */
			List<Field> shopFields = new ArrayList<Field>();
			for(Shop shop : shopList) {
				StringBuilder fieldPart = new StringBuilder();
				StringBuilder tempFieldPart;
				List<Offer> offersFromShop = offerList.stream()
						.filter(offer -> offer.getShopid().equals(shop.getId()))
						.collect(Collectors.toList());
	
				String fieldTopic = ":flag_" + shop.getCountry() + ": " + shop.getName();
				for(Offer offer : offersFromShop) {
					tempFieldPart = new StringBuilder();
					tempFieldPart.append(offer.getName());
					tempFieldPart.append(": [**" + String.format("%.2f", offer.getPrice()) + " " + shop.getCurrency() + "**]");
					tempFieldPart.append("(" + offer.getUrl() + ")\n");
					if((fieldPart.length() + tempFieldPart.length()) > 1024) {
						Field shopField = new Field(fieldTopic, fieldPart.toString(), false);
						shopFields.add(shopField);
						fieldPart = new StringBuilder();
						fieldTopic = "";
					}
					fieldPart.append(tempFieldPart);
				}

				Field shopField = new Field(fieldTopic, fieldPart.toString(), false);
				shopFields.add(shopField);
			}
			
			/* ================================================================================
			 * 5. Build and present embed
			 * ================================================================================ */
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("*" + ant.getName() + "*", "https://antwiki.org/wiki/" + ant.getName().replace(" ", "_"));
			eb.setColor(Antony.getBaseColor());
			eb.setDescription("Die folgenden Daten wurden von https://antcheck.info/ bereitgestellt.\n\n"
					+ "***Achtung:*** Die gelisteten Preise beinhalten keine Versandkosten und können je nach Shop unterschiedlich hoch ausfallen.");
			if (ant.getImage_url() != null && !ant.getImage_url().isEmpty()) {
				eb.setThumbnail(ant.getImage_url());
				
			}
			eb.setFooter("Preise und Verfügbarkeiten werden täglich mehrfach aktualisiert.\n"
					+ "Letzter erfolgreicher Abruf der Antcheck-Schnittstelle: "
					+ controller.getLastUpdatedDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + " Uhr");
			
			
			int ebCharCount = 0;
			int ebFieldCount = 0;
			for(Field field : shopFields) {
				if(((field.getName().length() + field.getValue().length() + ebCharCount) > 5000) || ebFieldCount == 25) {
					channel.sendMessageEmbeds(eb.build()).complete();
					eb.clearFields();
					ebCharCount = 0;
				}
				ebFieldCount++;
				ebCharCount += (field.getName().length() + field.getValue().length());
				eb.addField(field);
			}
			channel.sendMessageEmbeds(eb.build()).complete();
		}
	}
	
}