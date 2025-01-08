package bot.antony.commands.antcheck.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Currency {

	private String iso;
	private String symbol;
	private String name;
	private Float euro_rate;
	private String updated_at;

	public String getIso() {
		return iso;
	}

	public void setIso(String iso) {
		this.iso = iso;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Float getEuro_rate() {
		return euro_rate;
	}

	public void setEuro_rate(Float euro_rate) {
		this.euro_rate = euro_rate;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	@JsonIgnore
	public String print() {
		StringBuilder sb = new StringBuilder();
		sb.append(name + " (" + symbol + ", " + iso + ")");
		sb.append(", Umrechnungsfaktor: " + euro_rate);
		sb.append(", Stand: " + updated_at);
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Currency{" +
				"iso='" + iso + '\'' +
				", symbol='" + symbol + '\'' +
				", name='" + name + '\'' +
				", euro_rate=" + euro_rate +
				", updated_at='" + updated_at + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Currency currency = (Currency) o;
		return Objects.equals(iso, currency.iso) && Objects.equals(symbol, currency.symbol) && Objects.equals(name, currency.name) && Objects.equals(euro_rate, currency.euro_rate) && Objects.equals(updated_at, currency.updated_at);
	}

	@Override
	public int hashCode() {
		return Objects.hash(iso, symbol, name, euro_rate, updated_at);
	}
}
