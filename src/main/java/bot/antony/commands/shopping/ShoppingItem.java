package bot.antony.commands.shopping;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ShoppingItem {
	
	private int id = 0;
	private String shop;
	private String shopItemID;
	private String url;
	private String description;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ShoppingItem() {
		super();
	}
	
	public ShoppingItem(String shop, String shopItemID) {
		this.shop = shop;
		this.shopItemID = shopItemID;
	}
	
	public ShoppingItem(String shop, String shopItemID, String url, String description) {
		this.shop = shop;
		this.shopItemID = shopItemID;
		this.url = url;
		this.description = description;
	}
	
	public ShoppingItem(int id, String shop, String shopItemID, String url, String description) {
		this.id = id;
		this.shop = shop;
		this.shopItemID = shopItemID;
		this.url = url;
		this.description = description;
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public boolean equals(Object obj) {
		// If the object is compared with itself then return true
		if (obj == this) {
			return true;
		}
		// Check if obj is an instance of ShoppingItem or not "null instanceof [type]" also returns false
		if(!(obj instanceof ShoppingItem)) {
			return false;
		}
		// Typecast obj to ShoppingItem so that we can compare data
		ShoppingItem item = (ShoppingItem) obj;
		// Compare the ShoppingItem shop and id
		if(this.shop.equals(item.shop) && this.shopItemID.equals(item.shopItemID)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "ShoppingItem [id=" + id + ", shop=" + shop + ", shopItemID=" + shopItemID + ", url=" + url
				+ ", description=" + description + "]";
	}
	
	@JsonIgnore
	public String getAsListItem() {
		return "(" + id + ") " + description + " [" + shop + "; " + shopItemID + "; <" + url + ">]";
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

	public String getShopItemID() {
		return shopItemID;
	}

	public void setShopItemID(String shopItemID) {
		this.shopItemID = shopItemID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getShop() {
		return shop;
	}

	public void setShop(String shop) {
		this.shop = shop;
	}
	
}
