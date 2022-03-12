package bot.antony.db;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class DbController {
	private Configuration config;
	private SessionFactory sessionFactory;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public DbController() {
		super();
		config = new Configuration().configure();
		sessionFactory = null;
	}
	
	public DbController(String db) {
		this();
		setDb(db);
	}
	
	public DbController(Properties properties) {
		this();
		setConfigProperties(properties);
	}

	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------	
	public void setConfigProperties(Properties properties) {
		config.setProperties(properties);
	}
	
	public void setConfigProperty(String propertyName, String value) {
		config.setProperty(propertyName, value);
	}
	
	public void destroySessionFactory() {
		sessionFactory.close();
		sessionFactory = null;
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public String getDb() {
		return config.getProperty("hibernate.connection.url").substring(12);
	}

	public void setDb(String db) {
		if(db != null && !db.isEmpty()) {
			setConfigProperty("hibernate.connection.url", "jdbc:sqlite:" + db);
		}
	}

	public SessionFactory getSessionFactory() {
		if(sessionFactory == null) {
			sessionFactory = config.buildSessionFactory();
		}
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
