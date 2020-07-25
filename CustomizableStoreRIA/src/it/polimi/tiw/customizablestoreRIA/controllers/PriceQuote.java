package it.polimi.tiw.customizablestoreRIA.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

@WebServlet("/PriceQuote")
public class PriceQuote extends HttpServlet {
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
			throw new UnavailableException("Couldn't get db connection");
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		if (user == null || !user.getRole().equals("ROLE_EMPLOYEE")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Unauthorized");
			return;
		}

		String quoteIdString = request.getParameter("quoteId");
		String priceString = request.getParameter("price");
		if (quoteIdString == null || priceString == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing quote info");
			return;
		}

		int quoteId = Integer.parseInt(quoteIdString);
		BigDecimal price = new BigDecimal(priceString);

		EmployeeDAO employeeDAO = new EmployeeDAO(connection, user.getId());
		Quote quote = null;
		try {
			quote = employeeDAO.priceQuote(quoteId, price);
		} catch (SQLException e) {
			throw new ServletException(e);
		}

		if (quote == null) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Something went wrong");
		} else {
			String quoteJson = new Gson().toJson(quote);
			System.out.println(quoteJson);

			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			out.print(quoteJson);
			out.flush();
		}
	}

	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

}
