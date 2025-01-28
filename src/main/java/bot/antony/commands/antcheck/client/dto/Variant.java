package bot.antony.commands.antcheck.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Variant {

	private Integer id;
	private Integer product_id;
	private String title;
	private String description;
	private Float price;
	private boolean in_stock;
	private boolean is_active;
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

	public Integer getProduct_id() {
		return product_id;
	}

	public void setProduct_id(Integer product_id) {
		this.product_id = product_id;
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

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
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
		return "Variant{" +
				"id=" + id +
				", product_id=" + product_id +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", price=" + price +
				", in_stock=" + in_stock +
				", is_active=" + is_active +
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
		Variant variant = (Variant) o;
		return in_stock == variant.in_stock && is_active == variant.is_active && Objects.equals(id, variant.id) && Objects.equals(product_id, variant.product_id) && Objects.equals(title, variant.title) && Objects.equals(description, variant.description) && Objects.equals(price, variant.price) && Objects.equals(url, variant.url) && Objects.equals(created_at, variant.created_at) && Objects.equals(updated_at, variant.updated_at) && Objects.equals(changed_at, variant.changed_at) && Objects.equals(antcheck_url, variant.antcheck_url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, product_id, title, description, price, in_stock, is_active, url, created_at, updated_at, changed_at, antcheck_url);
	}
}
