package bot.antony.commands.antcheck.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

	private Integer id;
	private Integer shop_id;
	private String product_type;
	private Integer type_id;
	private String title;
	private String description;
	private boolean in_stock;
	private boolean is_active;
	private Float min_price;
	private Float max_price;
	private String currency_iso;
	private String url;
	private String created_at;
	private String updated_at;
	private String changed_at;
	private String antcheck_url;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getShop_id() {
		return shop_id;
	}

	public void setShop_id(Integer shop_id) {
		this.shop_id = shop_id;
	}

	public String getProduct_type() {
		return product_type;
	}

	public void setProduct_type(String product_type) {
		this.product_type = product_type;
	}

	public Integer getType_id() {
		return type_id;
	}

	public void setType_id(Integer type_id) {
		this.type_id = type_id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isIn_stock() {
		return in_stock;
	}

	public void setIn_stock(boolean in_stock) {
		this.in_stock = in_stock;
	}

	public boolean isIs_active() {
		return is_active;
	}

	public void setIs_active(boolean is_active) {
		this.is_active = is_active;
	}

	public Float getMin_price() {
		return min_price;
	}

	public void setMin_price(Float min_price) {
		this.min_price = min_price;
	}

	public Float getMax_price() {
		return max_price;
	}

	public void setMax_price(Float max_price) {
		this.max_price = max_price;
	}

	public String getCurrency_iso() {
		return currency_iso;
	}

	public void setCurrency_iso(String currency_iso) {
		this.currency_iso = currency_iso;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	public String getChanged_at() {
		return changed_at;
	}

	public void setChanged_at(String changed_at) {
		this.changed_at = changed_at;
	}

	public String getAntcheck_url() {
		return antcheck_url;
	}

	public void setAntcheck_url(String antcheck_url) {
		this.antcheck_url = antcheck_url;
	}

	@Override
	public String toString() {
		return "Product{" +
				"id=" + id +
				", shop_id=" + shop_id +
				", product_type='" + product_type + '\'' +
				", type_id=" + type_id +
				", title='" + title + '\'' +
				//", description='" + description + '\'' +
				", in_stock=" + in_stock +
				", is_active=" + is_active +
				", min_price=" + min_price +
				", max_price=" + max_price +
				", currency_iso='" + currency_iso + '\'' +
				", url='" + url + '\'' +
				", created_at='" + created_at + '\'' +
				", updated_at='" + updated_at + '\'' +
				", changed_at='" + changed_at + '\'' +
				", antcheck_url='" + antcheck_url + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Product product = (Product) o;
		return in_stock == product.in_stock && is_active == product.is_active && Objects.equals(id, product.id) && Objects.equals(shop_id, product.shop_id) && Objects.equals(product_type, product.product_type) && Objects.equals(type_id, product.type_id) && Objects.equals(title, product.title) && Objects.equals(description, product.description) && Objects.equals(min_price, product.min_price) && Objects.equals(max_price, product.max_price) && Objects.equals(currency_iso, product.currency_iso) && Objects.equals(url, product.url) && Objects.equals(created_at, product.created_at) && Objects.equals(updated_at, product.updated_at) && Objects.equals(changed_at, product.changed_at) && Objects.equals(antcheck_url, product.antcheck_url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, shop_id, product_type, type_id, title, description, in_stock, is_active, min_price, max_price, currency_iso, url, created_at, updated_at, changed_at, antcheck_url);
	}
}
