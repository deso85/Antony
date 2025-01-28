package bot.antony.commands.antcheck.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Specie {

	private Integer id;
	private String genus;
	private String species;
	private String comment;
	private String antcheck_url;
	private List<AntImage> images;

	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAntcheck_url() {
		return antcheck_url;
	}

	public void setAntcheck_url(String antcheck_url) {
		this.antcheck_url = antcheck_url;
	}

	@JsonIgnore
	public String getName() {
		return genus + " " + species;
	}

	public List<AntImage> getImages() {
		return images;
	}

	public void setImages(List<AntImage> images) {
		this.images = images;
	}

	@Override
	public String toString() {
		return "Specie{" +
				"id=" + id +
				", genus='" + genus + '\'' +
				", species='" + species + '\'' +
				", comment='" + comment + '\'' +
				", antcheck_url='" + antcheck_url + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Specie specie = (Specie) o;
		return Objects.equals(id, specie.id) && Objects.equals(genus, specie.genus) && Objects.equals(species, specie.species) && Objects.equals(comment, specie.comment) && Objects.equals(antcheck_url, specie.antcheck_url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, genus, species, comment, antcheck_url);
	}
}
