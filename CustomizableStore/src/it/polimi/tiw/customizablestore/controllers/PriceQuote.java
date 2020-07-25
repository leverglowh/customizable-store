package it.polimi.tiw.customizablestore.controllers;

import java.io.IOException;
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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.customizablestore.beans.Quote;
import it.polimi.tiw.customizablestore.beans.User;
import it.polimi.tiw.customizablestore.dao.EmployeeDAO;

@WebServlet("/PriceQuote")
public class PriceQuote extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
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
		String loginpath = getServletContext().getContextPath() + "/index.html";
		User user = null;
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			user = (User) session.getAttribute("user");
			if (!user.getRole().equals("ROLE_EMPLOYEE")) {
				response.sendRedirect(loginpath);
				return;
			}
		}

		String quoteId = request.getParameter("quoteId");
		if (quoteId == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing quote id");
			return;
		}

		int qtId = 0;
		try {
			qtId = Integer.parseInt(quoteId);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad quote id");
			return;
		}

		EmployeeDAO employeeDAO = new EmployeeDAO(connection, user.getId());

		Quote quote = new Quote();
		try {
			quote = employeeDAO.getQuoteDetails(qtId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure");
			return;
		}

		String path = "/WEB-INF/PriceQuote.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("quote", quote);
		templateEngine.process(path, ctx, response.getWriter());	
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
			if (!user.getRole().equals("ROLE_EMPLOYEE")) {
				response.sendRedirect(loginpath);
				return;
			}
		}

		String quoteIdString = request.getParameter("quoteId");
		String priceString = request.getParameter("price");
		if (quoteIdString == null || priceString == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing quote info");
			return;
		}

		int quoteId = Integer.parseInt(quoteIdString);
		BigDecimal price = new BigDecimal(priceString);

		if (price.compareTo(BigDecimal.ZERO) <= 0) {
			// price is less than or equal to 0: reject
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid price");
			return;
		}

		EmployeeDAO employeeDAO = new EmployeeDAO(connection, user.getId());
		try {
			employeeDAO.priceQuote(quoteId, price);
		} catch (SQLException e) {
			throw new ServletException(e);
		}

		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/GoToEmployeeHome";
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
