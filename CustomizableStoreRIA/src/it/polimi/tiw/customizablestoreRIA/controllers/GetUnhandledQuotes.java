package it.polimi.tiw.customizablestoreRIA.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.customizablestoreRIA.beans.Quote;
import it.polimi.tiw.customizablestoreRIA.beans.User;
import it.polimi.tiw.customizablestoreRIA.dao.EmployeeDAO;

@WebServlet("/GetUnhandledQuotes")
public class GetUnhandledQuotes extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			
			Class.forName(driver);
			
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get database connection");
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		if (user == null || !user.getRole().equals("ROLE_EMPLOYEE")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Unauthorized");
			return;
		}

		EmployeeDAO employeeDAO = new EmployeeDAO(connection, user.getId());
		List<Quote> unhandledQuotes = null;
		try {
			unhandledQuotes = employeeDAO.findUnhandledQuotes();
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure");
			return;
		}

		if (unhandledQuotes == null) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Something went wrong");
			return;
		} else {
			response.setStatus(HttpServletResponse.SC_OK);

			String uQuoteListJson = new Gson().toJson(unhandledQuotes);
			// System.out.println(productListJson);

			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			out.print(uQuoteListJson);
			out.flush();
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
