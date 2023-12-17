package bot.antony.commands.shopping;

import bot.antony.commands.types.ServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class ShoppingManagerCmd extends ServerCommand {
	ShoppingController controller;
	GuildMessageChannel channel;
	String[] userMessage;
	Message message;
	StringBuilder retVal;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ShoppingManagerCmd() {
		super();
		this.privileged = true;
		this.name = "shoppingmanager";
		this.description = "Verwaltung der Shopping-Liste";
		this.shortDescription = "Verwaltung der Shopping-Liste";
		this.example = "list Ausbruchschutz";
		
		this.cmdParams.put("count", "Zeigt die Anzahl der Kategorien und aller Items.");
		this.cmdParams.put("list [CategoryID | CategoryName]", "Listet alle Kategorien und Items auf. Wird eine Kategorie spezifiziert, werden nur dessen Inhalte angezeigt.");
		this.cmdParams.put("addcategory CategoryName", "Fügt eine Kategorie hinzu.");
		this.cmdParams.put("removecategory (CategoryID | CategoryName)", "Entfernt eine Kategorie und dessen Inhalte.");
		this.cmdParams.put("changecategoryid OldCategoryID NewCategoryID", "Ändert die Kategorie ID und damit die Sortierung.");
		this.cmdParams.put("additem CategoryID Shop ShopItemID URL Description", "Fügt einer Kategorie ein neues Item hinzu.");
		this.cmdParams.put("removeitem CategoryID ItemID", "Entfernt ein Item aus einer Kategorie.");
		this.cmdParams.put("changeitemid CategoryID OldItemID NewItemID", "Ändert die Item ID und damit die Sortierung.");
	}

	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		this.controller = new ShoppingController();
		this.channel = channel;
		this.userMessage = message.getContentDisplay().split(" ");
		this.message = message;
		this.retVal = new StringBuilder();
		
		if (userMessage.length > 1) {
			switch (userMessage[1].toLowerCase()) {
				case "count":
					performCount();
					break;
				case "list":
					performList();
					break;
				case "addcategory":
					performAddCategory();
					break;
				case "removecategory":
					performRemoveCategory();
					break;
				case "changecategoryid":
					performChangeCategoryID();
					break;
				case "additem":
					performAddItem();
					break;
				case "removeitem":
					performRemoveItem();
					break;
				case "changeitemid":
					performChangeItemID();
					break;
				default:
					printHelp(channel);
					break;
			}
			if(retVal.length() > 0) {
				channel.sendMessage(retVal.toString()).queue();
			}
		} else {
			printHelp(channel);
		}
	}
	
	public void performCount() {
		int catCount = 0;
		int itemCount = 0;
		
		if(controller.hasCategories()) { // has to count if there are categories
			catCount = controller.getCategories().size();
			
			for(ShoppingCategory cat : controller.getCategories()) { // check all items for each category
				if(cat.hasItems()) { // add item amount to counter
					itemCount += cat.getItems().size();
				}
			}
		}
		retVal.append("Es gibt **" + catCount + "** Kategorien und **" + itemCount + "** Items.");
	}
	
	public void performList() {
		String tempText = "";
		if(controller.hasCategories()) { // proceed if at least 1 category exists
			
			if(userMessage.length < 3) { // no category has been specified
				
				for(ShoppingCategory cat : controller.getCategories()) {
					tempText = "\n**" + cat.getIdAndName() + "**";
					if((retVal.length() + tempText.length() + 1) > 2000) {
						channel.sendMessage(retVal.toString()).queue();
						retVal = new StringBuilder();
					}
					retVal.append(tempText); // print category ID and name
					
					if(cat.hasItems()) { // proceed with items if existent
						retVal.append(":");
						
						for(ShoppingItem item : cat.getItems()) { // print each item
							tempText = "\n- " + item.getAsListItem();
							if((retVal.length() + tempText.length()) > 2000) {
								channel.sendMessage(retVal.toString()).queue();
								retVal = new StringBuilder();
							}
							retVal.append(tempText);
						}
					}
				}
			} else { // category has been specified
				ShoppingCategory cat = getCategoryByParam();
				
				if(cat != null) { //proceed if cat is set
					retVal.append("**" + cat.getIdAndName() + "**");
					
					if(cat.hasItems()) {
						for(ShoppingItem item : cat.getItems()) {
							tempText = "\n- " + item.getAsListItem();
							if((retVal.length() + tempText.length()) > 2000) {
								channel.sendMessage(retVal.toString()).queue();
								retVal = new StringBuilder();
							}
							retVal.append(tempText);
						}
					} else {
						retVal.append(" hat keine Items.");
					}
				} else {
					retVal.append("Die angegebene Kategorie \"**" + name + "**\" existiert nicht.");
				}
			}
		} else { // end if there are no categories
			retVal.append("Es gibt keine Kategorien und keine Items.");
		}
	}
	
	public void performAddCategory() {
		if (userMessage.length > 2) { // needs category name
			String name = message.getContentDisplay().substring(userMessage[0].length() + userMessage[1].length() + 2);
			
			if(controller.addCategory(name)) { // will only be added if no category with that name exists
				retVal.append("Kategorie \"**" + name + "**\" erfolgreich hinzugefügt.");
				
			} else { // category already exists
				retVal.append("Kategorie \"**" + name + "**\" existiert bereits.");
			}
			
		} else { // end because user didn't specify category name
			retVal.append("Bitte gib den Namen der Shopping-Kategorie als weiteren Parameter mit an.");
		}
	}
	
	public void performRemoveCategory() {
		if (userMessage.length > 2) { // needs category id or name
			ShoppingCategory category = getCategoryByParam();
			
			if(category != null) { // proceed if category is set
				if(controller.removeCategory(category)) {
					retVal.append("Kategorie \"**" + category.getIdAndName() + "**\" erfolgreich entfernt.");
				}
			} else { // category with given parameter doesn't exist
				retVal.append("Kategorie \"**" + message.getContentDisplay().substring(userMessage[0].length() + userMessage[1].length() + 2) + "**\" existiert nicht.");
			}
			
		} else { // end because user didn't specify category
			retVal.append("Bitte gib die ID oder den Namen der Shopping-Kategorie als weiteren Parameter mit an.");
		}
	}
	
	public void performChangeCategoryID() {
		if (userMessage.length > 3) { // needs category ID and new category ID
			
			if(Utils.isNumeric(userMessage[2]) && Utils.isNumeric(userMessage[3])) { // all parameters have to be numeric
				int catID = Integer.parseInt(userMessage[2]);
				int newCatID = Integer.parseInt(userMessage[3]);
				
				if(catID != newCatID) { // proceed if given parameters differ
					
					if(controller.hasCategory(catID)) { // proceed if category exists
						ShoppingCategory category = controller.getCategory(catID);
						
						if(controller.changeCategoryID(catID, newCatID)) { // id has been changed
							retVal.append("Die ID der Kategorie \"**" + category.getIdAndName() + "**\" wurde aktualisiert.");
							
						} else { // id hasn't been changed
							retVal.append("Die ID der Kategorie \"**" + category.getIdAndName() + "**\" konnte nicht aktualisiert werden.");
						}
						
					} else { // end because category doesn't exist
						retVal.append("Kategorie mit der ID \"**" + catID + "**\" existiert nicht.");
					}
					
				} else { // end because category id wouldn't be changed
					retVal.append("Die alte und neue ID sind identisch. Es muss nichts geändert werden.");
				}
				
			} else { // end bacause not every parameter is numeric
				retVal.append("Bitte gib folgendes an: OldCategoryID NewCategoryID");
			}
			
		} else { // end because user didn't enter all necessary parameters
			retVal.append("Bitte gib folgendes an: OldCategoryID NewCategoryID");
		}
	}
	
	public void performAddItem() {
		if (userMessage.length > 6) { // needs category ID, shop name, shop item id, url and description
			
			if(Utils.isNumeric(userMessage[2])) { // category ID has to be numeric
				int catID = Integer.parseInt(userMessage[2]);
				
				if(controller.hasCategory(catID)) { // proceed if category with given ID is available
					ShoppingCategory category = controller.getCategory(catID);
					String shop = userMessage[3];
					String shopItemID = userMessage[4];
					String url = userMessage[5];
					String description = message.getContentDisplay().substring(userMessage[0].length() + userMessage[1].length() + userMessage[2].length() + userMessage[3].length() + userMessage[4].length() + userMessage[5].length() + 6);

					if(controller.addItem(category, shop, shopItemID, url, description)) { // add item if it doesn't exist
						ShoppingItem item = category.getItem(shop, shopItemID);
						retVal.append("Der Kategorie \"**" + category.getIdAndName() + "**\" wurde das neue Item \"**" + item.getAsListItem() + "**\" hinzugefügt.");
						
					} else { // item already exists
						retVal.append("Die Kategorie \"**" + category.getIdAndName() + "**\" enthält dieses Item bereits.");
					}
					
				} else { // there is no category with the given ID
					retVal.append("Die angegebene Kategorie ID \"**" + catID + "**\" existiert nicht.");
				}
				
			} else { // parameters can't be correct because category id has to be numeric
				retVal.append("Bitte gib folgendes an: CategoryID Shop ID URL Description");
			}
			
		} else { // end because user didn't enter all necessary parameters
			retVal.append("Bitte gib folgendes an: CategoryID Shop ID URL Description");
		}
	}
	
	public void performRemoveItem() {
		if (userMessage.length > 3) { // needs category ID and item ID
			
			if(Utils.isNumeric(userMessage[2]) && Utils.isNumeric(userMessage[3])) { // both parameters have to be numeric
				int catID = Integer.parseInt(userMessage[2]);
				int itemID = Integer.parseInt(userMessage[3]);
				
				if(controller.hasCategory(catID)) { // proceed if category with given ID is available
					ShoppingCategory category = controller.getCategory(catID);
					
					if(category.hasItem(itemID)) { // proceed if category has item with given ID
						ShoppingItem item = category.getItem(itemID);
						
						if(controller.removeItem(category, item)) { // remove item from category. This triggers a resort of all items based by their ID
							retVal.append("Das Item \"**" + item.getAsListItem() + "**\" wurde aus der Kategorie \"**" + category.getIdAndName() + "**\" entfernt.");
							
						} else { // because item was found in the category this should never get triggered except it has been removed in a parallel session and therefore can't be removed now
							retVal.append("Das Item \"**" + item.getAsListItem() + "**\" konnte nicht aus der Kategorie \"**" + category.getIdAndName() + "**\" entfernt werden.");
						}
						
					} else { // the item doesn't exist in this category
						retVal.append("Die Kategorie \"**" + category.getIdAndName() + "**\" enthält dieses Item nicht.");
					}
					
				} else { // there is no category with the given ID
					retVal.append("Die angegebene Kategorie ID \"**" + catID + "**\" existiert nicht.");
				}
				
			} else { // parameters can't be correct because category id has to be numeric
				retVal.append("Bitte gib folgendes an: CategoryID ItemID");
			}
			
		} else { // end because user didn't enter all necessary parameters
			retVal.append("Bitte gib folgendes an: CategoryID ItemID");
		}
		
	}
	
	public void performChangeItemID() {
		if (userMessage.length > 4) { // needs category ID, item ID and new item ID
			
			if(Utils.isNumeric(userMessage[2]) && Utils.isNumeric(userMessage[3]) && Utils.isNumeric(userMessage[4])) { // all parameters have to be numeric
				int catID = Integer.parseInt(userMessage[2]);
				int itemID = Integer.parseInt(userMessage[3]);
				int newItemID = Integer.parseInt(userMessage[4]);
				
				if(itemID != newItemID) { // proceed if given parameters differ
					
					if(controller.hasCategory(catID)) { // proceed if category exists
						ShoppingCategory category = controller.getCategory(catID);
						
						if(category.hasItem(itemID)) { // proceed if item exists
							ShoppingItem item = category.getItem(itemID);
							
							if(controller.changeItemID(catID, itemID, newItemID)) { // id has been changed
								retVal.append("Die ID des Items \"**" + item.getAsListItem() + "**\" wurde aktualisiert.");
								
							} else { // id hasn't been changed
								retVal.append("Die ID des Items \"**" + item.getAsListItem() + "**\" konnte nicht aktualisiert werden.");
							}
							
						} else { // end because item doesn't exist
							retVal.append("Item mit der ID \"**" + itemID + "**\" existiert in der Kategorie \"**" + category.getIdAndName() + "**\" nicht.");
						}
						
					} else { // end because category doesn't exist
						retVal.append("Kategorie mit der ID \"**" + catID + "**\" existiert nicht.");
					}
					
				} else { // end because item id wouldn't be changed
					retVal.append("Die alte und neue ID sind identisch. Es muss nichts geändert werden.");
				}
				
			} else { // end bacause not every parameter is numeric
				retVal.append("Bitte gib folgendes an: CategoryID OldItemID NewItemID");
			}
			
		} else { // end because user didn't enter all necessary parameters
			retVal.append("Bitte gib folgendes an: CategoryID OldItemID NewItemID");
		}
	}
	
	public ShoppingCategory getCategoryByParam() {
		ShoppingCategory cat;
		String usrParam = message.getContentDisplay().substring(userMessage[0].length() + userMessage[1].length() + 2);
		
		if(Utils.isNumeric(usrParam)) { // is numeric (=id)
			cat = controller.getCategory(Integer.parseInt(usrParam));
		} else { // is String (=name)
			cat = controller.getCategory(usrParam);
		}
		
		return cat;
	}
	
}