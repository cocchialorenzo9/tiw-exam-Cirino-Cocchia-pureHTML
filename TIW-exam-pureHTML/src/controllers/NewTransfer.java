package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.CurrentAccountBean;
import daos.CurrentAccountDAO;
import daos.TransferDAO;

/**
 * Servlet implementation class NewTransfer
 */
@WebServlet("/NewTransfer")
public class NewTransfer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine = null;
	//private ServletContext context = null;
	

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
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int amount = 0;
		String reason = null;
		String CApayer = null;
		String CApayee = null;
		
		CurrentAccountBean payer = null;
		CurrentAccountBean payee = null;
		
		String path = "";
		//input controls section
		try {
			
			amount = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("amount")));
			reason = StringEscapeUtils.escapeJava(request.getParameter("reason"));
			CApayer = StringEscapeUtils.escapeJava(request.getParameter("CApayer"));
			CApayee = StringEscapeUtils.escapeJava(request.getParameter("CApayee"));
						
			if(reason == null || CApayer == null || CApayee == null) {
				response.getWriter().println("You can't pass null strings");
				throw new IllegalArgumentException();
			} else if(amount <= 0) {
				response.getWriter().println("You can't do a transfer with an amount less than or equals to 0");
				throw new IllegalArgumentException();
			} else if(reason.isEmpty() || CApayer.isEmpty() || CApayee.isEmpty()) {
				response.getWriter().println("You can't pass empty strings");
				throw new IllegalArgumentException();
			} else if(CApayer.length() != 4 || CApayee.length() != 4){
				response.getWriter().println("Current Account length is incorrect");
				throw new IllegalArgumentException();
			} else if (CApayer.contentEquals(CApayee)) {
				response.getWriter().println("Can't transfer an amuont from an account to the same account");
				throw new IllegalArgumentException();
			} else {
				CurrentAccountDAO caDao = new CurrentAccountDAO(connection);
				payer = caDao.getCAByCode(CApayer);
				payee = caDao.getCAByCode(CApayee);
				if(payer == null || payee == null) {
					response.getWriter().println("You inserted an invalid code");
					throw new IllegalArgumentException();
				} else if(payer.getCheck() < amount) {
					response.getWriter().println("Payer can't afford that amount of money");
					throw new IllegalArgumentException();
				}
			}
		} catch (IllegalArgumentException e) {
			path = "/Pages/PaymentKO.jsp";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("You passed an argument considered illegal");
			RequestDispatcher dispatcher = request.getRequestDispatcher(path);
			dispatcher.forward(request, response);
		}
		
		//transaction section
		//at this point I'm sure that input is coherent, now I have only to preserve DB integrity by Isolation (ACID)
		//and rollback in the cases in which there is some problem with DB (not due to input, but to connection)
		//https://dev.mysql.com/doc/refman/8.0/en/innodb-transaction-isolation-levels.html
		//MYSQL standard isolation is REPEATABLE_READ, it prevents dirty reads and non-repeatable reads, while allows
		//phantom reads
		//https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html		
		
		TransferDAO transferDao = new TransferDAO(connection);
		CurrentAccountDAO caDao = new CurrentAccountDAO(connection);
		Savepoint savepoint = null;
		
		try {
			connection.setAutoCommit(false);
			savepoint = connection.setSavepoint();
			
		} catch (SQLException e) {
			e.printStackTrace();
			path = "/Pages/PaymentKO.jsp";
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("There was an error while connecting to the server, retry later");
			RequestDispatcher dispatcher = request.getRequestDispatcher(path);
			dispatcher.forward(request, response);
		}
		
		try {
			int checkPayer = caDao.getCheckByCode(CApayer);
			int checkPayee = caDao.getCheckByCode(CApayee);
			boolean transPayer = caDao.updateCheckByAmount(CApayer, checkPayer - amount);
			boolean transPayee = caDao.updateCheckByAmount(CApayee, checkPayee + amount);
			boolean transRecord = transferDao.newTransfer(amount, reason, CApayer, CApayee);
			if(!transPayer || !transPayee || !transRecord) {
				throw new SQLException();
			} else {
				connection.commit();
				path = "/Pages/PaymentOK.jsp";
				response.setStatus(HttpServletResponse.SC_ACCEPTED);
				response.getWriter().println("Transfer has been registered");
				RequestDispatcher dispatcher = request.getRequestDispatcher(path);
				dispatcher.forward(request, response);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback(savepoint);
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		} finally {
			path = "/Pages/PaymentKO.jsp";
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			request.setAttribute("error", "There was an error while connecting to the server, retry later");
			response.getWriter().println("There was an error while connecting to the server, retry later");
			RequestDispatcher dispatcher = request.getRequestDispatcher(path);
			dispatcher.forward(request, response);
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
