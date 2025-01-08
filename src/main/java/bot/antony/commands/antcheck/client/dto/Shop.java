package bot.antony.commands.antcheck.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Shop {

	private Integer id;
	private String name;
	private String url;
	private String country;
	private String currency_iso;
	private boolean online;
	private boolean crawler_active;
	private String products_crawl_stats;
	private String antcheck_url;
	

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

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCurrency_iso() {
		return currency_iso;
	}

	public void setCurrency_iso(String currency_iso) {
		this.currency_iso = currency_iso;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public boolean isCrawler_active() {
		return crawler_active;
	}

	public void setCrawler_active(boolean crawler_active) {
		this.crawler_active = crawler_active;
	}

	public String getProducts_crawl_stats() {
		return products_crawl_stats;
	}

	public void setProducts_crawl_stats(String products_crawl_stats) {
		this.products_crawl_stats = products_crawl_stats;
	}

	public String getAntcheck_url() {
		return antcheck_url;
	}

	public void setAntcheck_url(String antcheck_url) {
		this.antcheck_url = antcheck_url;
	}

	@JsonIgnore
	public String getIdAndName() {
		return "(" + id + ") " + name;
	}

	@Override
	public String toString() {
		return "Shop{" +
				"id=" + id +
				", name='" + name + '\'' +
				", url='" + url + '\'' +
				", country='" + country + '\'' +
				", currency_iso='" + currency_iso + '\'' +
				", online=" + online +
				", crawler_active=" + crawler_active +
				", products_crawl_stats='" + products_crawl_stats + '\'' +
				", internal_url='" + antcheck_url + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Shop shop = (Shop) o;
		return online == shop.online && crawler_active == shop.crawler_active && Objects.equals(id, shop.id) && Objects.equals(name, shop.name) && Objects.equals(url, shop.url) && Objects.equals(country, shop.country) && Objects.equals(currency_iso, shop.currency_iso) && Objects.equals(products_crawl_stats, shop.products_crawl_stats) && Objects.equals(antcheck_url, shop.antcheck_url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, url, country, currency_iso, online, crawler_active, products_crawl_stats, antcheck_url);
	}
}
