package rpc;

import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;

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
import external.TicketMasterClient;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search") // appears in http request url. 且唯一对应一个servlet.
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchItem() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	// GET method of http. 即 RESTFUL API.
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
		
		String userId = session.getAttribute("user_id").toString();
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		// Term can be empty or null.
		String term = request.getParameter("keyword");
		// default is MySQL.
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			List<Item> items = connection.searchItems(lat, lon, term);
			Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
			
			JSONArray array = new JSONArray();
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				// to check if the search result item is actually a favorite item of the user
				obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
				array.put(obj);
				//array.put(item.toJSONObject());
			}
			RpcHelper.writeJsonArray(response, array);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
		// response.getWriter().append("Served at: ").append(request.getContextPath());
		// response.setContentType("text/html"); // tell client the format of the
		// response.
//		response.setContentType("application/json");
//		PrintWriter writer = response.getWriter();
//		JSONObject obj = new JSONObject();
//		JSONArray arr = new JSONArray();
//		writer.print("<html><body>");
//		writer.print("<h1><span style=\"color:blue\">Hello World</h1>");
//		writer.print("</body></html>");
//		writer.close();
//		if (request.getParameter("username") != null) {
//			//http://localhost:8080/UrEvent/search?username=Zj
//			String username = request.getParameter("username");
//			writer.println("<html><body>");
//			writer.println("<h1>Hello " + username + "</h1>");
//			writer.println("</body></html>");
//			writer.close();
//		}
//		if (request.getParameter("username") != null) {
//			//http://localhost:8080/UrEvent/search?username=Zj
//			String username = request.getParameter("username");
//			try {
//				// construct the JSON object.
//				// JSON object内部维护了一个private HashMap.
//				obj.put("username", username);
//				writer.print(obj);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//		writer.close();
//		try {
//			arr.put(new JSONObject().put("username", "abcd"));
//			arr.put(new JSONObject().put("username", "1234"));
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		writer.print(arr);
//		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	// POST method of http
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
