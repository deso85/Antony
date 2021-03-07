package bot.antony.db.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import bot.antony.db.ConfigurationEntity;

public class ConfigurationDAO implements ConfigurationDAOInterface {
	@Inject
	private EntityManager em;
	
	@Override
	public List<ConfigurationEntity> selectAll(){
		return em.createNamedQuery(ConfigurationEntity.QUERY_FIRST_SQL, ConfigurationEntity.class).getResultList();
	}
	
	@Override
	public ConfigurationEntity findById(String id) {
		return em.find(ConfigurationEntity.class, id);
	}
	
	@Override
	public void save(ConfigurationEntity ce) {
		em.persist(ce);
	}
	
	@Override
	public void update(ConfigurationEntity ce) {
		em.merge(ce);
	}
	
	@Override
	public void remove(ConfigurationEntity ce) {
		em.remove(ce);
	}
	
	@Override
	public void removeById(String id) {
		ConfigurationEntity ce = this.findById(id);
		this.remove(ce);
	}
}
