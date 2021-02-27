package bot.antony.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbController {
	private String dbpath;
	private static Connection connection;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public DbController() {
		super();
	}
	
	public DbController(String dbpath) {
		setDbpath(dbpath);
	}

	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public void connect() {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbpath);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void init() {
		connect();
		close();
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public String getDbpath() {
		return dbpath;
	}

	public void setDbpath(String dbpath) {
		this.dbpath = dbpath;
	}

	public static Connection getConnection() {
		return connection;
	}

	public static void setConnection(Connection connection) {
		DbController.connection = connection;
	}
	
}
