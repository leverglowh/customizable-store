package it.polimi.tiw.customizablestore.controllers;

import java.io.IOException;
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

import it.polimi.tiw.customizablestore.beans.User;
import it.polimi.tiw.customizablestore.dao.ClientDAO;

@WebServlet("/CreateQuote")
public class CreateQuote extends HttpServlet {
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
		String loginpath = getServletContext().getContextPath() + "/index.html";
		User user = null;
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			user = (User) session.getAttribute("user");
			if (!user.getRole().equals("ROLE_CLIENT")) {
				response.sendRedirect(loginpath);
				return;
			}
		}
		
		String productId = request.getParameter("productId");
		String baseOptionId = request.getParameter("baseOptionId");
		String[] optionList = request.getParameterValues("optionIds");
		if (productId == null || baseOptionId == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing quote data");
			return;
		}
		
		int prodId = 0;
		int optLength = optionList != null ? optionList.length : 0;
		int[] optIds = new int[optLength + 1];
		try {
			prodId = Integer.parseInt(productId);
			optIds[0] = Integer.parseInt(baseOptionId);
			for (int i = 0; i < optLength; i ++) {
				optIds[i + 1] = Integer.parseInt(optionList[i]);
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad quote data");
			return;
		}
		
		ClientDAO clientDAO = new ClientDAO(connection, user.getId());
		try {
			clientDAO.createQuote(prodId, optIds);
		} catch (SQLException e) {
			// throw new ServletException(e);
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure");
			return;
		}

		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/GoToClientHome";
		response.sendRedirect(path);
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
