package bot.antony.commands.antcheck.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Shop {

	private Integer id;

	private String name;

	private String url;
	
	private String shipping;

	private String country;

	private String currency;
	
	private boolean active_listed;
	
	private String last_update;
	
	private Discount discount;
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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
	
	public String getShipping() {
		return shipping;
	}

	public void setShipping(String shipping) {
		this.shipping = shipping;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCurrency() {
		return currency;
	}
	
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public boolean isActive_listed() {
		return active_listed;
	}

	public void setActive_listed(boolean active_listed) {
		this.active_listed = active_listed;
	}

	public String getLast_update() {
		return last_update;
	}

	public void setLast_update(String last_update) {
		this.last_update = last_update;
	}

	public Discount getDiscount() {
		return discount;
	}

	public void setDiscount(Discount discount) {
		this.discount = discount;
	}

	@JsonIgnore
	public String getIdAndName() {
		return "(" + id + ") " + name;
	}
	
	@Override
	public String toString() {
		return "Shop [id=" + id + ", name=" + name + ", url=" + url + ", shipping=" + shipping + ", country=" + country
				+ ", currency=" + currency + ", active_listed=" + active_listed + ", last_update=" + last_update
				+ ", discount=" + discount + "]";
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
