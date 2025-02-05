package bot.antony.commands;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import bot.antony.Antony;
import bot.antony.commands.antcheck.AntcheckController;
import bot.antony.commands.antcheck.client.dto.Shop;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class ShopsCmd extends ServerCommand {
	DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	AntcheckController controller;
	GuildMessageChannel channel;
	String lastUpdated;
	String[] userMessage;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ShopsCmd() {
		super();
		this.privileged = false;
		this.name = "shops";
		this.description = "Auflistung der Shops, die auf Antcheck gelistet sind.";
		this.shortDescription = "Auflistung aller bekannten Shops.";
		this.example = "de";
		this.cmdParams.put("[*Country ISO Code*]", "Gibt eine Liste aller Shops aus. Der ISO Code eines Landes kann zum Filtern der Ausgabe genutzt werden.");
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		//set variables
		controller = Antony.getAntcheckController();
		lastUpdated = controller.getLastUpdatedDateTime().format(dtFormatter);
		this.channel = channel;
		userMessage = message.getContentDisplay().split(" ");
		String[] locales = Locale.getISOCountries();
		
		// set filter for country based output
		String countryFilter = "";
		if(userMessage.length > 1){
			if(userMessage[1].length() == 2) {
				countryFilter = userMessage[1].toLowerCase();
			}
		}
		
		// general information
		StringBuilder returnString = new StringBuilder();
		returnString.append("Informiert euch bitte ausreichend über die Shops, bevor ihr etwas kauft.\n\n");
		returnString.append("Die Liste der Shops wird von <https://antcheck.info/> bereitgestellt. Schaut dort gerne direkt vorbei.\n");
		
		// iterate through shops by country
		for (String countryCode : locales) {
			Locale locale = new Locale("", countryCode);
			String ccode = locale.getCountry().toLowerCase();
			String cname = locale.getDisplayCountry();
			
			// if country based filter is not set or current country matches the filter
			if(countryFilter == "" || ccode.equals(countryFilter)) {
				
				// get all non blacklisted shops located in the country
				List<Shop> localShops = controller.getNonBLOnlineShops().stream()
					.filter(shop -> shop.getCountry().equals(ccode))
					.collect(Collectors.toList());
				
				// print country flag, name and code as headline for the output
				String tempString = "";
				if(localShops.size() > 0) {
					tempString = "\n:flag_" + ccode + ": " + cname + " (" + ccode.toUpperCase() + ")\n";
					if((returnString.length() + tempString.length()) > 1000) {
						channel.sendMessage(returnString.toString()).queue();
						returnString = new StringBuilder();
					}
					returnString.append(tempString);
				}
				// print all shops located in the country
				for(Shop shop : localShops) {
					tempString = "- " + shop.getName() + " <" + shop.getUrl() + ">\n";
					if((returnString.length() + tempString.length()) > 1000) {
						channel.sendMessage(returnString.toString()).queue();
						returnString = new StringBuilder();
					}
					returnString.append(tempString);
				}
			}
		}
		
		channel.sendMessage(returnString.toString()).queue();
	}

}