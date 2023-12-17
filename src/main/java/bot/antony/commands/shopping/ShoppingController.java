package bot.antony.commands.shopping;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.utils.Utils;

/**
 * Controller to regularly check if ant species are available and to infom user
 */
public class ShoppingController {
	private String subdir = "shopping" + File.separator;
	private String savefile = "shoppingLists.json";
	private List<ShoppingCategory> categories = new ArrayList<ShoppingCategory>();

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ShoppingController() {
		load();
		//Antony.getLogger().info("Created antcheck availability controller.");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	
	// Persistence
	@SuppressWarnings("unchecked")
	private void load() {
		categories = (List<ShoppingCategory>) Utils.loadJSONData(subdir, savefile, new TypeReference<List<ShoppingCategory>>(){}, categories);
	}
	
	private void save() {
		Utils.saveJSONData(subdir, savefile, categories);
	}
	
	// Category
	public boolean hasCategories() {
		if(categories.size() > 0) {
			return true;
		}
		return false;
	}
	
	public boolean hasCategory(int id) {
		for(ShoppingCategory category : categories) {
			if(category.getId() == id) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasCategory(String name) {
		for(ShoppingCategory category : categories) {
			if(category.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public ShoppingCategory getCategory(int id) {
		for(ShoppingCategory category : categories) {
			if(category.getId() == id) {
				return category;
			}
		}
		return null;
	}
	
	public ShoppingCategory getCategory(String name) {
		for(ShoppingCategory category : categories) {
			if(category.getName().equals(name)) {
				return category;
			}
		}
		return null;
	}
	
	public boolean addCategory(String name) {
		int catId = getCategories().size() + 1;
		while(hasCategory(catId)) {
			catId++;
		}
		ShoppingCategory cat = new ShoppingCategory(catId, name);
		Boolean retVal = categories.add(cat);
		save();
		return retVal;
	}
	
	public boolean removeCategory(ShoppingCategory category) {
		Boolean retVal = categories.remove(category);
		for(int i = 0; i < categories.size(); i++) {
			categories.get(i).setId(i+1);
		}
		save();
		return retVal;
	}
	
	public boolean changeCategoryID(int categoryID, int newCategoryID) {
		if(hasCategory(categoryID)) {
			
			if(!hasCategory(newCategoryID)) {
				getCategory(categoryID).setId(newCategoryID);
			} else {
			
				if(categoryID < newCategoryID) {
					for(int i = categoryID; i < newCategoryID; i++) {
						if(hasCategory(i+1)) {
							ShoppingCategory a = getCategory(i);
							ShoppingCategory b = getCategory(i+1);
							a.setId(i+1);
							b.setId(i);
						} else {
							getCategory(i).setId(i+1);
						}
					}
				} else {
					for(int i = categoryID; i > newCategoryID; i--) {
						if(hasCategory(i-1)) {
							ShoppingCategory a = getCategory(i);
							ShoppingCategory b = getCategory(i-1);
							a.setId(i-1);
							b.setId(i);
						} else {
							getCategory(i).setId(i-1);
						}
					}
				}
			
			}
			
			setCategories(getCategories().stream()
					.sorted(Comparator.comparing(ShoppingCategory::getId))
					.collect(Collectors.toList()));
			
			for(int i = 0; i < getCategories().size() ; i++) {
				getCategories().get(i).setId(i+1);
			}
			
			save();
			return true;
		}
		return false;
	}
	
	// Item
	public boolean addItem(ShoppingCategory category, String shop, String shopItemID, String url, String description) {
		if(hasCategory(category.getId())) {
			int itemID = category.getItems().size() + 1;
			while(category.hasItem(itemID)) {
				itemID++;
			}
			ShoppingItem item = new ShoppingItem(itemID, shop, shopItemID, url, description);
			Boolean retVal = category.addItem(item);
			save();
			return retVal;
		}
		return false;
	}
	
	public boolean removeItem(ShoppingCategory category, ShoppingItem item) {
		if(category.removeItem(item)) {
			save();
			return true;
		}
		return false;
	}
	
	public boolean changeItemID(int categoryID, int itemID, int newItemID) {
		if(hasCategory(categoryID)) {
			ShoppingCategory category = getCategory(categoryID);
			
			if(category.hasItem(itemID)) {
				
				if(!category.hasItem(newItemID)) {
					category.getItem(itemID).setId(newItemID);
				} else {
				
					if(itemID < newItemID) {
						for(int i = itemID; i < newItemID; i++) {
							if(category.hasItem(i+1)) {
								ShoppingItem a = category.getItem(i);
								ShoppingItem b = category.getItem(i+1);
								a.setId(i+1);
								b.setId(i);
							} else {
								category.getItem(i).setId(i+1);
							}
						}
					} else {
						for(int i = itemID; i > newItemID; i--) {
							if(category.hasItem(i-1)) {
								ShoppingItem a = category.getItem(i);
								ShoppingItem b = category.getItem(i-1);
								a.setId(i-1);
								b.setId(i);
							} else {
								category.getItem(i).setId(i-1);
							}
						}
					}
					
				}
				
				category.setItems(category.getItems().stream()
						.sorted(Comparator.comparing(ShoppingItem::getId))
						.collect(Collectors.toList()));
				
				for(int i = 0; i < category.getItems().size() ; i++) {
					category.getItems().get(i).setId(i+1);
				}
				
				save();
				return true;
			}
		}
		return false;
	}

	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public List<ShoppingCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<ShoppingCategory> categories) {
		this.categories = categories;
	}
	
}