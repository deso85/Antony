package bot.antony.db;

import java.util.HashMap;
import java.util.Map;

import bot.antony.Antony;


public class PersistenceContextBridgeMock extends AbstractPersistenceContextBridgeMock implements EntityManagerBridge {
 
  @Override
  protected Map<String, Object> getSessionFactoryProperties() {
    final Map<String, Object> properties = new HashMap<>();
    properties.put("hibernate.connection.url", "jdbc:sqlite:" + Antony.getProperty("sqlite.db.path"));
    //properties.put("hibernate.connection.username", "username"); // replace
    //properties.put("hibernate.connection.password", "password"); // replace
    properties.put("hibernate.connection.driver", "org.sqlite.JDBC");
    properties.put("hibernate.dialect", "org.sqlite.hibernate.dialect.SQLiteDialect");
    properties.put("hibernate.show_sql", "false");
    properties.put("hibernate.format_sql", "false");
    properties.put("hibernate.id.new_generator_mappings", "false");
    properties.put("hibernate.use_outer_join", "true");
    properties.put("hibernate.default_batch_fetch_size", "20");
    properties.put("hibernate.jdbc.fetch_size", "20");
    properties.put("hibernate.jdbc.batch_size", "100");
    properties.put("hibernate.max_fetch_depth", "3");
//    properties.put("hibernate.hbm2ddl.auto", "create-drop");
//    properties.put("hibernate.hbm2ddl.auto", "create");
    properties.put("hibernate.hbm2ddl.auto", "update");


    // add other required configurations
    return properties;
  }
  

  
}

