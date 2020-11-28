package bot.antony.antcheck.client.dto;

public class Shop {

	private String id;

	private String name;

	private String url;

	private String shippingurl;

	private String country;

	private String priceinfo;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getShippingurl() {
		return shippingurl;
	}

	public void setShippingurl(String shippingurl) {
		this.shippingurl = shippingurl;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPriceinfo() {
		return priceinfo;
	}

	public void setPriceinfo(String priceinfo) {
		this.priceinfo = priceinfo;
	}

	@Override
	public String toString() {
		return "Shop [id=" + id + ", name=" + name + ", url=" + url + ", shippingurl=" + shippingurl + ", country="
				+ country + ", priceinfo=" + priceinfo + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Shop other = (Shop) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
