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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

        List<Shop> matchingShops = controller.getNonBLOnlineShops().stream()
                .filter(shop -> shop.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());

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
        if (matchingShops.size() == 1) {
            Shop shop = matchingShops.get(0);

            List<Offer> offers = shop.getOffers();
            if (offers == null || offers.isEmpty()) {
                channel.sendMessage("Der Shop **" + shop.getName() + "** hat aktuell keine Angebote.").queue();
                return;
            }

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Angebote von " + shop.getName(), shop.getUrl()); // Shop-Name + klickbarer Link
            eb.setFooter("Daten bereitgestellt von antcheck.info");

            int fieldCounter = 0;
            StringBuilder fieldContent = new StringBuilder();

            for (Offer offer : offers.stream()
                    .sorted(Comparator.comparing(o -> o.getSpecie().getName(), String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList())) {

                // Beispieltext pro Angebot
                String line = "• **" + offer.getSpecie().getName() + "** – " + offer.getPrice() + " " + offer.getCurrency() + "\n";

                // prüfen, ob dieses Field das Limit sprengen würde
                if (fieldContent.length() + line.length() >= 1024) {
                    eb.addField(new Field("Angebote (" + (fieldCounter + 1) + ")", fieldContent.toString(), false));
                    fieldContent = new StringBuilder();
                    fieldCounter++;
                }

                fieldContent.append(line);

                // wenn wir schon 25 Felder hätten → Embed abschicken und neuen starten
                if (fieldCounter >= 25) {
                    channel.sendMessageEmbeds(eb.build()).queue();
                    eb = new EmbedBuilder();
                    eb.setTitle("Angebote von " + shop.getName(), shop.getUrl());
                    eb.setFooter("Daten bereitgestellt von antcheck.info");
                    fieldCounter = 0;
                }
            }

            // Rest anhängen
            if (fieldContent.length() > 0) {
                eb.addField(new Field("Angebote (" + (fieldCounter + 1) + ")", fieldContent.toString(), false));
            }

            channel.sendMessageEmbeds(eb.build()).queue();
            return;
        }


    }
}