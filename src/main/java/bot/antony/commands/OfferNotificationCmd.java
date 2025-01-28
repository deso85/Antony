package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.antcheck.AntcheckController;
import bot.antony.commands.antcheck.AntcheckNotificationController;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.commands.types.ServerCommand;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class OfferNotificationCmd extends ServerCommand {

	List<Specie> specieList = new ArrayList<Specie>();
	GuildMessageChannel channel;
	AntcheckController antcheckController;
	AntcheckNotificationController antcheckNotificationController;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public OfferNotificationCmd() {
		super();
		this.privileged = false;
		this.name = "offernotification";
		this.description = "Mit diesem Befehl kannst du dich darüber informieren lassen, wenn eine Ameisenart von gelisteten Shops angeboten wird.";
		this.shortDescription = "Benachrichtigung über Verfügbarkeit einer Ameisenart.";
		this.example = "Camponotus fulvopilosus";
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		this.antcheckController = Antony.getAntcheckController();
		this.antcheckNotificationController = Antony.getAntcheckNotificationController();
		this.channel = channel;
		String[] userMessage = message.getContentDisplay().replaceAll("\\s+", " ").trim().split(" ");
		String searchString = "";
		
		/* ================================================================================
		 * 1. Search for the ant
		 * ================================================================================ */
		if (userMessage.length > 2) { //Genus and specie name are given
			specieList = antcheckController.findAnt(userMessage[1], userMessage[2]);
			searchString = userMessage[1] + " " + userMessage[2];
		} else if(userMessage.length > 1) { //only genus or specie name is given
			specieList = antcheckController.findAnt(userMessage[1]);
			searchString = userMessage[1];
		} else {
			printHelp(channel);
			return;
		}
		
		if(specieList.isEmpty()) { //found no ants with the given search parameters
			sendNoAntMessage(searchString);
		} else if(specieList.size() == 1) { //found an ants with the given search parameters
			if(antcheckController.getFilteredAvailableAntProducts(specieList.get(0)).isEmpty()) {
				antcheckNotificationController.addAvailabilityCheck(specieList.get(0), Antony.getGuildController().getGuildData(message.getGuild()), new UserData(member));
				message.reply("Du wirst einmalig darüber informiert, wenn die Ameisenart ***" + specieList.get(0).getName() + "*** im Verkauf gefunden wird.").queue();
				antcheckNotificationController.run();
			} else {
				message.reply("Die Ameisenart ***" + specieList.get(0).getName() + "*** wird bereits verkauft."
						+ "\nBitte nutze für weitere Details den Befehl: ***" + Antony.getCmdPrefix() + "sells " + specieList.get(0).getName() + "***").queue();
			}
		} else { //found multiple ants with the given search parameters
			sendTooManySpeciesFound(specieList.size() ,searchString);
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
	
	private void sendTooManySpeciesFound(int count, String searchString) {
		StringBuilder returnString = new StringBuilder();
		
		returnString.append("Es wurden " + count + " Ameisenarten mit ");
		if(searchString.contains(" ")) {
			returnString.append("den Suchbegriffen ");
		} else {
			returnString.append("dem Suchbegriff ");
		}
		returnString.append("\"" + searchString + "\" gefunden.\n");
		returnString.append("Bitte schränke die Suche ein und versuche es erneut.");
		
		channel.sendMessage(returnString.toString()).queue();
	}

}