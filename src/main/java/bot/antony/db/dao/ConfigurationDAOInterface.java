package bot.antony.db.dao;

import java.util.List;

import bot.antony.db.ConfigurationEntity;

public interface ConfigurationDAOInterface {

	List<ConfigurationEntity> selectAll();

	ConfigurationEntity findById(String id);

	void save(ConfigurationEntity ce);

	void update(ConfigurationEntity ce);

	void remove(ConfigurationEntity ce);

	void removeById(String id);

}