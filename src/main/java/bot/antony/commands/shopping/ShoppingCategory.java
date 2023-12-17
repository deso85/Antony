package bot.antony.commands.shopping;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ShoppingCategory {

	private int id = 1;
	private String name = "";
	private List<ShoppingItem> items = new ArrayList<ShoppingItem>();

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ShoppingCategory() {
		super();
	}
	
	public ShoppingCategory(String name) {
		this.name = name;
	}
	
	public ShoppingCategory(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public ShoppingCategory(String name, List<ShoppingItem> items) {
		this.name = name;
		this.items = items;
	}
	
	public ShoppingCategory(int id, String name, List<ShoppingItem> items) {
		this.id = id;
		this.name = name;
		this.items = items;
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public boolean addItem(ShoppingItem item) {
		if(!hasItem(item)) {
			return items.add(item);
		}
		return false;
	}
	
	public boolean removeItem(ShoppingItem item) {
		if(item != null) {
			Boolean retVal = items.remove(item);
			for(int i = 0; i < items.size(); i++) {
				items.get(i).setId(i+1);
			}
			return retVal;
		}
		return false;
	}
	
	public boolean hasItems() {
		if(items.size() > 0) {
			return true;
		}
		return false;
	}
	
	public boolean hasItem(ShoppingItem item) {
		return getItems().contains(item);
	}
	
	public boolean hasItem(String shop, String shopItemID) {
		for(ShoppingItem item : items) {
			if(item.getShop().equals(shop) && item.getShopItemID().equals(shopItemID)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasItem(int id) {
		for(ShoppingItem item : items) {
			if(item.getId() == id) {
				return true;
			}
		}
		return false;
	}
	
	@JsonIgnore
	public ShoppingItem getItem(String shop, String shopItemID) {
		for(ShoppingItem item : items) {
			if(item.getShop().equals(shop) && item.getShopItemID().equals(shopItemID)) {
				return item;
			}
		}
		return null;
	}
	
	@JsonIgnore
	public ShoppingItem getItem(int id) {
		for(ShoppingItem item : items) {
			if(item.getId() == id) {
				return item;
			}
		}
		return null;
	}
	
	@JsonIgnore
	public String getIdAndName() {
		return "(" + id + ") " + name;
	}
	
	@Override
	public String toString() {
		return "ShoppingCategory [id=" + id + ", name=" + name + "]";
	}

	@Override
	public boolean equals(Object obj) {
		// If the object is compared with itself then return true
		if (obj == this) {
			return true;
		}
		// Check if obj is an instance of ShoppingCategory or not "null instanceof [type]" also returns false
		if(!(obj instanceof ShoppingCategory)) {
			return false;
		}
		// Typecast obj to ShoppingCategory so that we can compare data
		ShoppingCategory cat = (ShoppingCategory) obj;
		// Compare the ShoppingCategory name
		if(this.name.equals(cat.name)) {
			return true;
		}
		return false;
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<ShoppingItem> getItems() {
		return items;
	}
	
	public void setItems(List<ShoppingItem> items) {
		this.items = items;
	}
	
}
