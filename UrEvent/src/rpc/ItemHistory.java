package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ItemHistory() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	// given user_id, return this user's favorite items.
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		JSONObject objSession = new JSONObject();
		if (session == null) {
			try {
				objSession.put("status", "Need to login");
				RpcHelper.writeJsonObject(response, objSession);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			response.setStatus(403);
			return;
		}
		
		String userId = request.getParameter("user_id");
		JSONArray array = new JSONArray();

		DBConnection conn = DBConnectionFactory.getConnection();
		try {
			Set<Item> items = conn.getFavoriteItems(userId);
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.append("favorite", true);
				array.put(obj);
			}
			RpcHelper.writeJsonArray(response, array);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response) { 'user_id':'1111', 'favorite' : [ 'item_id1', 'item_id2' ] }
	 */
	// set user's favorite items.
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		JSONObject objSession = new JSONObject();
		if (session == null) {
			try {
				objSession.put("status", "Need to login");
				RpcHelper.writeJsonObject(response, objSession);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			response.setStatus(403);
			return;
		}

		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			JSONArray array = input.getJSONArray("favorite");
			List<String> itemIds = new ArrayList<>();
			for (int i = 0; i < array.length(); ++i) {
				itemIds.add(array.getString(i));
			}
			connection.setFavoriteItems(userId, itemIds);
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	// unset user's favorite items.
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		JSONObject objSession = new JSONObject();
		if (session == null) {
			try {
				objSession.put("status", "Need to login");
				RpcHelper.writeJsonObject(response, objSession);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			response.setStatus(403);
			return;
		}

		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			JSONArray array = input.getJSONArray("favorite");
			List<String> itemIds = new ArrayList<>();
			for (int i = 0; i < array.length(); ++i) {
				itemIds.add(array.getString(i));
			}
			connection.unsetFavoriteItems(userId, itemIds);
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}
}
