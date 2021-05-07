package bot.antony.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "configuration")
@NamedQueries({
		@NamedQuery(name = ConfigurationEntity.QUERY_FIRST_SQL, query = "SELECT c from ConfigurationEntity c")})
public class ConfigurationEntity {
	public static final String QUERY_FIRST_SQL = "ConfigurationEntity.SelectAll";
	@Id
	private String id;
	
	@Column(name = "configkey", length = 255)
	private String key;
	//private String value;
	
	
	public ConfigurationEntity() {
		super();
	}
	
	public ConfigurationEntity(String id, String key) {
		super();
		this.id = id;
		this.key = key;
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
		ConfigurationEntity other = (ConfigurationEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/*public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}*/
	
	
}
