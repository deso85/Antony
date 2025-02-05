package bot.antony.commands;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import bot.antony.Antony;
import bot.antony.commands.antcheck.AntcheckController;
import bot.antony.commands.antcheck.client.dto.*;
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
			if(!controller.getFilteredAvailableAntProducts(specie).isEmpty()) {
				speciesWithOffers.add(specie);
			}
		}

		if(speciesWithOffers.isEmpty()) {
			channel.sendMessage("Mit der Suche nach \"" + searchString + "\" wurden " + specieList.size() + " Ameisenarten gefunden, aber leider werden davon aktuell keine verkauft.").queue();
		} else if(speciesWithOffers.size() == 1) {
			channel.sendMessage("Mit der Suche nach \"" + searchString + "\" wurden " + specieList.size() + " Ameisenarten gefunden, gibt es aber nur für ***" + speciesWithOffers.get(0).getName() + "*** Angebote.").queue();
			showOffersForAnt(speciesWithOffers.get(0));
		} else {
			returnString.append("Folgende " + speciesWithOffers.size() + " Ameisenarten wurden im Verkauf gefunden:\n\n");
			for(Specie specie : speciesWithOffers.stream()
					.sorted(Comparator.comparing(Specie::getName, String.CASE_INSENSITIVE_ORDER))
					.collect(Collectors.toList())) {
				if((returnString.length() + specie.getName().length() + 40) >= 2000) {
					channel.sendMessage(returnString.toString()).queue();
					returnString = new StringBuilder();
				}
				returnString.append("*• ");
				returnString.append(specie.getName());
				returnString.append("*\n");
			}
			returnString.append("\nBitte schränke die Suche weiter ein.");
			channel.sendMessage(returnString.toString()).queue();
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
		List<Product> products  = new ArrayList<>();
		List<Shop> shops = new ArrayList<>();
		List<Variant> variants = new ArrayList<>();

		// search for products with the given ant
		products = controller.getFilteredAvailableAntProducts(ant);
		// filter out products from shops which are not online or blacklisted
		shops = controller.getNonBLOnlineShops();
		if(products.isEmpty()) {
			channel.sendMessage("Es werden aktuell keine *" + ant.getName() + "* verkauft.").queue();
		} else {
			// get only relevant shops
			shops = controller.getShopsFromProducts(products).stream()
					.sorted(Comparator.comparing(Shop::getName, String.CASE_INSENSITIVE_ORDER))
					.collect(Collectors.toList());
			/* ================================================================================
			 * Combine shops with offers and variants in embed fields
			 * ================================================================================ */
			List<Field> shopFields = new ArrayList<Field>();
			for(Shop shop : shops) {
				StringBuilder fieldPart = new StringBuilder();
				StringBuilder tempFieldPart;
				List<Product> productsFromShop = products.stream()
						.filter(product -> product.getShop_id().equals(shop.getId()))
						.collect(Collectors.toList());

				String fieldTopic = ":flag_" + shop.getCountry() + ": " + shop.getName();
				for(Product product : productsFromShop) {
					variants = controller.getAvailableProductVariants(product);

					fieldPart.append("**" + product.getTitle() + "**\n");

					int counter = 1;
					for(Variant variant : variants) {
						tempFieldPart = new StringBuilder();

						if (!variant.getTitle().isEmpty()) {
							tempFieldPart.append(variant.getTitle());
						} else {
							tempFieldPart.append("Variante " + counter);
						}

						tempFieldPart.append(": [**" + String.format("%.2f", variant.getPrice()) + " " + shop.getCurrency_iso() + "**]");
						tempFieldPart.append("(" + variant.getUrl() + ")");

						// show EUR price
						if(!shop.getCurrency_iso().equals("EUR")) {
							Currency currency = controller.getCurrency(shop.getCurrency_iso());
							tempFieldPart.append(" *(ca. " + String.format("%.2f", variant.getPrice()/currency.getEuro_rate()) + " EUR)*");
						}

						tempFieldPart.append("\n");

						if ((fieldPart.length() + tempFieldPart.length()) > 1024) {
							Field shopField = new Field(fieldTopic, fieldPart.toString(), false);
							shopFields.add(shopField);
							fieldPart = new StringBuilder();
							fieldTopic = "";
						}
						fieldPart.append(tempFieldPart);
						counter++;
					}
				}

				Field shopField = new Field(fieldTopic, fieldPart.toString(), false);
				shopFields.add(shopField);
			}

			/* ================================================================================
			 * Build and present embed
			 * ================================================================================ */
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("*" + ant.getName() + "*", "https://antwiki.org/wiki/" + ant.getName().replace(" ", "_"));
			eb.setColor(Antony.getBaseColor());
			eb.setDescription("Die folgenden Daten wurden von https://antcheck.info/ bereitgestellt.\n\n"
					+ "***Achtung:*** Die gelisteten Preise beinhalten keine Versandkosten und können je nach Shop unterschiedlich hoch ausfallen.");
			//ToDo: Images dont work from API and have to be replaced by another source
			if (!ant.getImages().isEmpty() && !ant.getImages().get(0).getUrl().isEmpty()) {
				String imageUrl = ant.getImages().get(0).getUrl();
                try {
                    if(bot.antony.utils.Utils.isValidURL(imageUrl)) {
                        eb.setThumbnail(imageUrl);
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
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