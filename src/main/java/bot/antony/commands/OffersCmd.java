package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.antcheck.AntcheckController;
import bot.antony.commands.antcheck.client.dto.*;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OffersCmd extends ServerCommand {

	GuildMessageChannel channel;
	AntcheckController controller;

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public OffersCmd() {
		super();
		this.privileged = false;
		this.name = "offers";
		this.description = "Listet alle Angebote eines Shops auf. Die Daten werden von https://antcheck.info zur Verfügung gestellt. Vielen Dank hierfür!";
		this.shortDescription = "Listet alle Angebote eines Shops auf.";
		this.example = "Antstore";
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		this.controller = Antony.getAntcheckController();
		this.channel = channel;
        String content = message.getContentDisplay()
                .replaceAll("\\s+", " ")
                .trim();

        // Make sure there is a search string to search for a shop
        String[] parts = content.split(" ", 2);
        String searchString = parts.length > 1 ? parts[1].trim() : null;

        if (searchString == null || searchString.isEmpty()) {
            channel.sendMessage("Bitte gib einen Shopnamen an. Beispiel: `!offers Antstore`").queue();
            return;
        }

        List<Shop> allShops = controller.getNonBLOnlineShops();
        List<Shop> matchingShops;

        // First: check for exact matches (case-insensitive, trimmed)
        List<Shop> exactMatches = allShops.stream()
                .filter(shop -> shop.getName().equalsIgnoreCase(searchString.trim()))
                .collect(Collectors.toList());

        if (!exactMatches.isEmpty()) {
            // If there are exact matches, prefer them
            matchingShops = exactMatches;
        } else {
            // If no exact matches are found, fall back to partial matches
            matchingShops = allShops.stream()
                    .filter(shop -> shop.getName().toLowerCase().contains(searchString.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // No shop found with the given search string. Send error message.
        if (matchingShops.isEmpty()) {
            channel.sendMessage("Es wurde kein Shop mit dem Namen \"" + searchString + "\" gefunden.").queue();
            return;
        }

        // Multiple shops with given search string found. Will name them and ask for more specific search.
        if (matchingShops.size() > 1) {
            StringBuilder returnString = new StringBuilder();
            returnString.append("Folgende ").append(matchingShops.size()).append(" Shops wurden gefunden:\n\n");

            for (Shop shop : matchingShops.stream()
                    .sorted(Comparator.comparing(Shop::getName))
                    .collect(Collectors.toList())) {

                String line = "• *" + shop.getName() + "*\n";

                // Prüfen, ob das Hinzufügen die 2000 Zeichen sprengt
                if ((returnString.length() + line.length()) >= 2000) {
                    channel.sendMessage(returnString.toString()).queue();
                    returnString = new StringBuilder();
                }

                returnString.append(line);
            }

            returnString.append("\nBitte schränke die Suche weiter ein.");
            channel.sendMessage(returnString.toString()).queue();

            return;
        }

        // Found a shop. Send known offers.
        Shop shop = matchingShops.get(0);
        List<Product> products = controller.getAvailableProductsByShop(shop);

        if (products.isEmpty()) {
            channel.sendMessage("Der Shop **" + shop.getName() + "** hat aktuell keine Ameisen im Angebot oder wünscht, nicht aufgelistet zu werden.").queue();
            return;
        }

        // Put offers into Embed
        List<Field> productFields = new ArrayList<>();
        for (Product product : products) {
            StringBuilder fieldPart = new StringBuilder();
            List<Variant> variants = controller.getAvailableProductVariants(product);

            int counter = 1;
            for (Variant variant : variants) {
                StringBuilder temp = new StringBuilder();

                // Variant title or "Variante x"
                String variantTitle = !variant.getTitle().isEmpty() ? variant.getTitle() : "Variante " + counter;
                temp.append(variantTitle)
                        .append(": [**")
                        .append(String.format("%.2f", variant.getPrice()))
                        .append(" ").append(shop.getCurrency_iso()).append("**](")
                        .append(variant.getUrl()).append(")");

                // Calculate € price if necessary
                if (!shop.getCurrency_iso().equals("EUR")) {
                    Currency currency = controller.getCurrency(shop.getCurrency_iso());
                    temp.append(" *(ca. ")
                            .append(String.format("%.2f", variant.getPrice() / currency.getEuro_rate()))
                            .append(" EUR)*");
                }

                temp.append("\n");

                // Flush if field is full
                if ((fieldPart.length() + temp.length()) > 1024) {
                    productFields.add(new Field(product.getTitle(), fieldPart.toString(), false));
                    fieldPart = new StringBuilder();
                }

                fieldPart.append(temp);
                counter++;
            }

            productFields.add(new Field(product.getTitle(), fieldPart.toString(), false));
        }

        // Build Embed
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(":flag_" + shop.getCountry() + ": " + shop.getName(), shop.getUrl());
        eb.setColor(Antony.getBaseColor());
        eb.setDescription("Alle aktuellen Ameisen-Angebote von **" + shop.getName() + "**.\n"
                + "Quelle: https://antcheck.info\n"
                + "***Achtung:*** Die gelisteten Preise beinhalten keine Versandkosten und können je nach Shop unterschiedlich hoch ausfallen.");

        eb.setFooter("Preise und Verfügbarkeiten werden täglich mehrfach aktualisiert.\n"
                + "Letzter erfolgreicher Abruf der Antcheck-Schnittstelle: "
                + controller.getLastUpdatedDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + " Uhr");

        // Send embed fields (check limits)
        int ebCharCount = 0;
        int ebFieldCount = 0;
        for (Field field : productFields) {
            if ((field.getName().length() + field.getValue().length() + ebCharCount) > 5000 || ebFieldCount == 25) {
                channel.sendMessageEmbeds(eb.build()).complete();
                eb.clearFields();
                ebCharCount = 0;
                ebFieldCount = 0;
            }
            eb.addField(field);
            ebFieldCount++;
            ebCharCount += (field.getName().length() + field.getValue().length());
        }
        channel.sendMessageEmbeds(eb.build()).queue();

    }
}