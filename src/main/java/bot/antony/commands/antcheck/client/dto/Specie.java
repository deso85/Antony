package bot.antony.commands.antcheck.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Specie {

	private Integer id;

	private String genus;

	private String species;

	private String image_url;
	
	private String image_caption;

	private String image_licence;
	
	private String url;
	
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

	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public String getImage_caption() {
		return image_caption;
	}

	public void setImage_caption(String image_caption) {
		this.image_caption = image_caption;
	}

	public String getImage_licence() {
		return image_licence;
	}

	public void setImage_licence(String image_licence) {
		this.image_licence = image_licence;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@JsonIgnore
	public String getName() {
		return genus + " " + species;
	}
	
	@Override
	public String toString() {
		return "Specie [id=" + id + ", genus=" + genus + ", species=" + species + ", image_url=" + image_url
				+ ", image_caption=" + image_caption + ", image_licence=" + image_licence + ", url=" + url + "]";
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
		Specie other = (Specie) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
