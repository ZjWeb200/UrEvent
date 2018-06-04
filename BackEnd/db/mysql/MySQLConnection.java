package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import external.TicketMasterAPI;

//db.mysql package contains mysql version of DBConnection implementation
public class MySQLConnection implements DBConnection {
	private Connection conn;

	// Constructor
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Close the connection
	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
	}
	
	// favorites 存入 db 中的history 表中
	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}
		
		try {
			// history表 有三个attributes，此处只设置前两个。
			String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}
		
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
			if (conn == null) {
				return new HashSet<>();
			}
			Set<String> favoriteItemIds = new HashSet<>();
			
			try {
				String sql = "SELECT item_id FROM history WHERE user_id = ?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, userId);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					String itemId = rs.getString("item_id");
					favoriteItemIds.add(itemId);
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return favoriteItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if(conn == null) {
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?"; // * 取 items table中的所有 columns。 用 where 提取 item_id column
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId: itemIds) {
				 stmt.setString(1, itemId);
				 ResultSet rs = stmt.executeQuery(); // rs is an iterator, rs points to a record
				 
				 Item.ItemBuilder builder = new Item.ItemBuilder();
				 
				 while(rs.next()) {
					 builder.setItemId(rs.getString("item_id"));
					 builder.setName(rs.getString("name"));
					 builder.setAddress(rs.getString("address"));
					 builder.setImageUrl(rs.getString("image_url"));
					 builder.setUrl(rs.getString("url"));
					 builder.setCategories(getCategories(itemId));
					 builder.setDistance(rs.getDouble("distance"));
					 builder.setRating(rs.getDouble("rating"));
					 
					 favoriteItems.add(builder.build()); // 根据 itemId创建一个我们自己定义的item object然后存入favorite itmes set
				 }
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteItems;
	}

	//根据 itemId获得该item的categories
	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			return new HashSet<>();
		}
		HashSet<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category FROM categories WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, itemId);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term); // find the searching contents
		for (Item item : items) {
			saveItem(item);     // save each item into the database
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		if (conn == null) {
			return;
		}
		try {
			// Use PreparedStatement to avoid SQL injection
			// 还可以用 preparedstatement 循环插入多个items，template固定了。
			//items有7项，故7个问号
			String sql = "INSERT IGNORE INTO items VALUES(?, ?, ?, ?, ?, ?, ?)";  // 不可插入重复的primary key
			PreparedStatement stmt = conn.prepareStatement(sql);
			// set 上面的问号
			stmt.setString(1, item.getItemId());
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress());
			stmt.setString(5, item.getImageUrl());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7, item.getDistance());
			stmt.execute();
			
			//把每个item的categories存入categories关系表
			sql = "INSERT IGNORE INTO categories VALUES (?, ?)";
			stmt = conn.prepareStatement(sql);
			for (String category : item.getCategories()) {
				stmt.setString(1, item.getItemId());
				stmt.setString(2, category);
				stmt.execute();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			return null;
		}
		String name = "";
		try {
			String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				name = String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id from users WHERE user_id = ? and password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
