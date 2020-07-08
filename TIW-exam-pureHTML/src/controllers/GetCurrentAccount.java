package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import daos.CurrentAccountDAO;
import daos.TransferDAO;
import beans.CurrentAccountBean;
import beans.TransferBean;


/**
 * Servlet implementation class GetCurrentAccount
 */
@WebServlet("/GetCurrentAccount")
public class GetCurrentAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine = null;
	private ServletContext context = null;
	

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		try {
			context = getServletContext();
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
		int CAid = -1;
		try {
			CAid = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("CAid")));
		} catch (IllegalArgumentException e) {
			System.out.println("CAid were not passed as parameter, searching in cookies");
			CAid = getIdFromCookies(request.getCookies());
			if(CAid == -1) {
				response.getWriter().println("Wrong CAid number format for the request");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}
		
		CurrentAccountDAO caDao = new CurrentAccountDAO(connection);
		TransferDAO transferDao = new TransferDAO(connection);
		List<TransferBean> allTransfers = new ArrayList<>();
		
		CurrentAccountBean CA = caDao.getCAById(CAid);
		
		
		if(CA == null) {
			response.getWriter().println("There was a server error, retry later");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		
		try {
			allTransfers = transferDao.getTransfersByCAId(CA.getCAcode());
		} catch (SQLException e) {
			e.printStackTrace();
			response.getWriter().println("There was a server error, retry later");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		request.getCookies();
		Cookie cookieCA = new Cookie("idCurrentAccount", Integer.toString(CA.getIdcurrentAccount()));
		response.addCookie(cookieCA);
		//request.getSession().setAttribute("CA", CA);
		
		String path = "/Pages/AccountState.html";
		
		final WebContext ctx = new WebContext(request, response, context, request.getLocale());
		ctx.setVariable("thyCA", CA);
		ctx.setVariable("allTransfers", allTransfers);
		templateEngine.process(path, ctx, response.getWriter());
		
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
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
	
	private int getIdFromCookies(Cookie[] cookies) {
		int ret = -1;
		
		for(Cookie c : cookies) {
			if(c.getName().contentEquals("idCurrentAccount")) {
				try {
					ret = Integer.parseInt(c.getValue());
				} catch (NumberFormatException e) {
					System.out.println("cookie value unparsable");
				}
				
				return ret;
			}
		}
		
		return ret;
	}

}
