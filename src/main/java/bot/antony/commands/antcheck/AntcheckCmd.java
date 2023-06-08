package bot.antony.commands.antcheck;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.antcheck.client.dto.Shop;
import bot.antony.commands.types.ServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class AntcheckCmd extends ServerCommand {
	DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	AntcheckController controller;
	GuildMessageChannel channel;
	String lastUpdated;
	String[] userMessage;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AntcheckCmd() {
		super();
		this.privileged = true;
		this.name = "antcheck";
		this.description = "Administration der Antcheck Schnittstelle";
		this.shortDescription = "Administration der Antcheck Schnittstelle";
		this.example = "shops list";
		this.cmdParams.put("offers count", "Gibt die Anzahl Angebote aus.");
		this.cmdParams.put("shops (count | list)", "Gibt die Anzahl oder eine Liste der Shops aus.");
		this.cmdParams.put("shops blacklist (count | list | add | remove) (ShopId | ShopName)", "Interaktion mit der Blacklist von Shops.");
		this.cmdParams.put("species count", "Gibt die Anzahl Ameisen Arten aus.");
		this.cmdParams.put("status", "Gibt den aktuellen Status der Schnittstelle aus.");
		this.cmdParams.put("update [offers | shops | species]", "Aktualisiert die spezifizierten Daten. Ohne weitere Parameter werden alle Daten aktualisiert.");
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		controller = Antony.getAntcheckController();
		lastUpdated = controller.getLastUpdatedDateTime().format(dtFormatter);
		this.channel = channel;
		userMessage = message.getContentDisplay().split(" ");
		
		if (userMessage.length > 1) {
			switch (userMessage[1].toLowerCase()) {
				case "offers":
					performOffers();
					break;
				case "shops":
					performShops();
					break;
				case "species":
					performSpecies();
					break;
				case "status":
					performStatus();
					break;
				case "update":
					performUpdate();
					break;
				default:
					performStatus();
					break;
			}
		} else {
			printHelp(channel);
		}
	}
	
	public void performOffers() {
		if (userMessage.length > 2) {
			switch (userMessage[2].toLowerCase()) {
				case "count":
					channel.sendMessage("Antcheck hat **" + controller.getOffers().size() + "** Angebote gelistet."
							+ "\n*Stand: " + lastUpdated + " Uhr*").queue();
					break;
			}
		} else {
			printHelp(channel);
		}
	}
	
	public void performShops() {
		StringBuilder returnString = new StringBuilder();
		if (userMessage.length > 2) {
			switch (userMessage[2].toLowerCase()) {
			case "blacklist":
				performShopsBlacklist();
				break;	
			case "count":
				channel.sendMessage("Antcheck hat **" + controller.getShops().size() + "** Shops gelistet.\n*Stand: " + lastUpdated + " Uhr*").queue();
				break;
			case "list":
				returnString.append("Folgende Shops sind gelistet:\n");
				for(Shop shop : controller.getShops()) {
					if((returnString.length() + shop.getIdAndName().length() + 4) > 1000) {
						channel.sendMessage(returnString.toString()).queue();
						returnString = new StringBuilder();
					}
					returnString.append("- " + shop.getIdAndName() + "\n");
				}
				channel.sendMessage(returnString.toString()).queue();
				break;
			}
		} else {
			printHelp(channel);
		}
	}
	
	public void performShopsBlacklist() {
		StringBuilder returnString = new StringBuilder();
		if (userMessage.length > 3) {
			switch (userMessage[3].toLowerCase()) {
			case "add":
				if (userMessage.length > 4) {
					List<Shop> shops = new ArrayList<Shop>();
					if(Utils.isNumeric(userMessage[4].toLowerCase())) {
						shops = controller.addBlShop(Integer.parseInt(userMessage[4].toLowerCase()));
					} else {
						shops = controller.addBlShop(userMessage[4].toLowerCase());
					}
					if(shops.size() > 0) {
						for(Shop shop : shops) {
							returnString.append("- " + shop.getIdAndName() + "\n");
						}
						if(shops.size() > 1) {
							channel.sendMessage("Folgende " + shops.size() + " Shops wurden der Blacklist hinzugefügt:"
									+ "\n" + returnString.toString()).queue();
						} else {
							channel.sendMessage("**" + shops.get(0).getIdAndName() + "** wurde der Blacklist hinzugefügt").queue();
						}
					} else {
						channel.sendMessage("Es wurden keine Shops der Blacklist hinzugefügt.").queue();
					}
				}
				break;	
			case "count":
					channel.sendMessage("Es sind **" + controller.getBlShops().size() + "** Shops auf der Blacklist.").queue();;
					break;
			case "list":
				if(controller.getBlShops().size() > 0) {
					for(Shop shop : controller.getBlShops()) {
						returnString.append("- " + shop.getIdAndName() + "\n");
					}
					channel.sendMessage("Folgende " + controller.getBlShops().size() + " Shops sind auf der Blacklist:"
							+ "\n" + returnString.toString()).queue();
				} else {
					channel.sendMessage("Es sind keine Shops auf der Blacklist.").queue();
				}
				break;
			case "remove":
				if (userMessage.length > 4) {
					List<Shop> shops = new ArrayList<Shop>();
					if(Utils.isNumeric(userMessage[4].toLowerCase())) {
						shops = controller.removeBlShop(Integer.parseInt(userMessage[4].toLowerCase()));
					} else {
						shops = controller.removeBlShop(userMessage[4].toLowerCase());
					}
					if(shops.size() > 0) {
						for(Shop shop : shops) {
							returnString.append("- " + shop.getIdAndName() + "\n");
						}
						if(shops.size() > 1) {
							channel.sendMessage("Folgende " + shops.size() + " Shops wurden von der Blacklist entfernt:"
									+ "\n" + returnString.toString()).queue();
						} else {
							channel.sendMessage("**" + shops.get(0).getIdAndName() + "** wurde von der Blacklist entfernt.").queue();
						}
					} else {
						channel.sendMessage("Es wurden keine Shops von der Blacklist entfernt.").queue();
					}
				}
				break;
			}
		} else {
			printHelp(channel);
		}
	}
	
	public void performSpecies() {
		if (userMessage.length > 2) {
			switch (userMessage[2].toLowerCase()) {
				case "count":
					channel.sendMessage("Antcheck hat **" + controller.getSpecies().size() + "** Arten gelistet."
							+ "\n*Stand: " + lastUpdated + " Uhr*").queue();
					break;
			}
		} else {
			printHelp(channel);
		}
	}
	
	public void performStatus() {
		StringBuilder returnString = new StringBuilder();
		if(Utils.getAntCheckClient().getShops() != null && Utils.getAntCheckClient().getShops().size() > 0) {
			returnString.append("\n**API:** erreichbar");
		} else {
			returnString.append("\n**API:** nicht erreichbar");
		}
		returnString.append("\n**Letzter erfolgreicher Datenabruf:** " + lastUpdated + " Uhr");
		returnString.append("\n**Arten:** " + controller.getSpecies().size());
		returnString.append("\n**Shops:** " + controller.getShops().size());
		returnString.append("\n* *Davon auf der Blacklist: " + controller.getBlShops().size() + "*");
		returnString.append("\n**Angebote:** " + controller.getOffers().size());
		channel.sendMessage(returnString.toString()).queue();
	}
	
	public void performUpdate() {
		if (userMessage.length > 2) {
			switch (userMessage[2].toLowerCase()) {
				case "offers":
					controller.updateOffers();
					channel.sendMessage("Die Angebote wurden aktualisiert.").queue();
					break;
				case "shops":
					controller.updateShops();
					channel.sendMessage("Die Shops wurden aktualisiert.").queue();
					break;
				case "species":
					controller.updateSpecies();
					channel.sendMessage("Die Ameisen-Arten wurden aktualisiert.").queue();
					break;
			}
		} else {
			controller.updateData();
			channel.sendMessage("Alle Daten wurden aktualisiert.").queue();
		}
	}

}