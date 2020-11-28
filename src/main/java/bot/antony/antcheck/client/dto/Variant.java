package bot.antony.antcheck.client.dto;

public class Variant {
	
	private String speciesid;

	private String shopid;

	private String price;

	private String name;

	private String url;

	public String getSpeciesid() {
		return speciesid;
	}

	public void setSpeciesid(String speciesid) {
		this.speciesid = speciesid;
	}

	public String getShopid() {
		return shopid;
	}

	public void setShopid(String shopid) {
		this.shopid = shopid;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "Variant [speciesid=" + speciesid + ", shopid=" + shopid + ", price=" + price + ", name=" + name
				+ ", url=" + url + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((speciesid == null) ? 0 : speciesid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variant other = (Variant) obj;
		if (speciesid == null) {
			if (other.speciesid != null)
				return false;
		} else if (!speciesid.equals(other.speciesid))
			return false;
		return true;
	}

}
