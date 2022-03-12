package bot.antony.db.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "guild")
@NamedQueries({
	@NamedQuery(name = GuildEntity.QUERY_FIRST_SQL, query = "SELECT c from GuildEntity c")
})
public class GuildEntity {
	public static final String QUERY_FIRST_SQL = "GuildEntity.SelectAll";

	private long id;
	private String name;

	public GuildEntity() {

	}

	public GuildEntity(long id, String name) {
		this.id = id;
		this.name = name;
	}

	@Id
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
