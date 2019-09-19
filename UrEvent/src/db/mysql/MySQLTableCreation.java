package db.mysql;

import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;

public class MySQLTableCreation {
	// Run this as Java application to reset db schema.
	// Idealy run only once in this project's whole lifetime.
	// Every time run this will clear the database.
	// When the project is deployed online, we will never run this anymore.
	public static void main(String[] args) {
		try {
			// Connect to the MySQL server.
			System.out.println("Connecting to " + MySQLDBUtil.URL);
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			Connection conn = DriverManager.getConnection(MySQLDBUtil.URL);

			if (conn == null) {
				return;
			}

			// Drop tables in case they exist.
			Statement statement = conn.createStatement();
			// syntax in sql to drop a table. in java it's just a string.
			String sql = "DROP TABLE IF EXISTS categories";
			statement.executeUpdate(sql); // the actual step to drop the table in the db

			sql = "DROP TABLE IF EXISTS history";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS items";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS users";
			statement.executeUpdate(sql);

			// Next, Create new tables
			// syntax in sql to create a table.
			// VARCHAR255: 255 character maximum in the number of characters in a VARCHAR (String) column
			sql = "CREATE TABLE items (" + "item_id VARCHAR(255) NOT NULL," + "name VARCHAR(255)," + "rating FLOAT,"
					+ "address VARCHAR(255)," + "image_url VARCHAR(255)," + "url VARCHAR(255)," + "distance FLOAT,"
					+ "local_date VARCHAR(255)," + "PRIMARY KEY (item_id)" + ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE users (" + "user_id VARCHAR(255) NOT NULL," + "password VARCHAR(255) NOT NULL,"
					+ "first_name VARCHAR(255)," + "last_name VARCHAR(255)," + "PRIMARY KEY (user_id)" + ")";
			statement.executeUpdate(sql);

			// 注明primary key and foreign key.
			sql = "CREATE TABLE categories (" + "item_id VARCHAR(255) NOT NULL," + "category VARCHAR(255) NOT NULL,"
					+ "PRIMARY KEY (item_id, category)," + "FOREIGN KEY (item_id) REFERENCES items(item_id)" + ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE history (" + "user_id VARCHAR(255) NOT NULL," + "item_id VARCHAR(255) NOT NULL,"
					+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+ "PRIMARY KEY (user_id, item_id)," + "FOREIGN KEY (user_id) REFERENCES users(user_id),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id)" + ")";
			statement.executeUpdate(sql);

			// insert fake user user id: 1111 password: 1298
			// just for testing purpose
//			sql = "INSERT INTO users VALUES('1111', '1298', 'Zj', 'Zheng')";
//			statement.executeUpdate(sql);
			
			conn.close();
			System.out.println("Import done successfully");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
