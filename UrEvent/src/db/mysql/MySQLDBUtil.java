package db.mysql;

public class MySQLDBUtil {
	private static final String HOSTNAME = "localhost";
	private static final String PORT_NUM = "3306";  // corresponds to the port of mysql.
	public static final String DB_NAME = "laiproject";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";
	// url to connect to the db server.
	public static final String URL = "jdbc:mysql://"
			+ HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";
}
