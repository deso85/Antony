package bot.antony.commands.antcheck.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Shop {

	private Integer id;

	private String name;

	private String shipping;

	private String country;

	private String currency;

	private Boolean crawler_active;
	
	private String last_update;
	

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

	public Boolean getCrawler_active() {
		return crawler_active;
	}

	public void setCrawler_active(Boolean crawler_active) {
		this.crawler_active = crawler_active;
	}

	public String getLast_update() {
		return last_update;
	}

	public void setLast_update(String last_update) {
		this.last_update = last_update;
	}

	@JsonIgnore
	public String getIdAndName() {
		return "(" + id + ") " + name;
	}
	
	@Override
	public String toString() {
		return "Shop [id=" + id + ", name=" + name + ", shipping=" + shipping + ", country=" + country + ", currency="
				+ currency + ", crawler_active=" + crawler_active + ", last_update=" + last_update + "]";
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
