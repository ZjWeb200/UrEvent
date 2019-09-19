package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    // Check if already logged in. If yes, return the user's first name and last name.
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			// 此处false是不会创建一个新的session.
			// if there is no session, we do NOT create one.
			HttpSession session = request.getSession(false);
			JSONObject obj = new JSONObject();
			// if there is session, then return userId and the user's name.
			if (session != null) {
				String userId = session.getAttribute("user_id").toString();
				obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
			} else {
				// we do not have a session. need to login.
				obj.put("status", "Invalid Session, need to login");
				response.setStatus(403);
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
			
			JSONObject obj = new JSONObject();
			// if userId exists and the password matches.
			if (connection.verifyLogin(userId, password)) {
				// create a new session.
				HttpSession session = request.getSession();
				// set the user_id attribute to this session.
				session.setAttribute("user_id", userId);
				session.setMaxInactiveInterval(600); // 600 sec有效
				obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
			} else {
				obj.put("status", "User Doesn't Exist or Wrong Password");
				response.setStatus(401);
			}
			RpcHelper.writeJsonObject(response, obj);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}

	}

}
