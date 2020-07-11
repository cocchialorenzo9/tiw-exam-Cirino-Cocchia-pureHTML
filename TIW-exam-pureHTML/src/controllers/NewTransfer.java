package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.CurrentAccountBean;
import beans.UserBean;
import daos.CurrentAccountDAO;
import daos.TransferDAO;
import daos.UserDAO;
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
		boolean inputIsOk = false;
		String errorMessage = "";
		
		float amount = 0;
		String reason = null;
		String userCodePayee = null;
		String CApayer = null;
		String CApayee = null;
		
		CurrentAccountBean payer = null;
		CurrentAccountBean payee = null;
		
		String path = "";
		String errorPath = "/ErrorPages/PaymentKO.jsp";
		
		RequestDispatcher errorDispatcher = request.getRequestDispatcher(errorPath);
		
		CurrentAccountDAO caDao = new CurrentAccountDAO(connection);

		//input controls section
		try {
			
			amount = Float.parseFloat(StringEscapeUtils.escapeJava(request.getParameter("amount")));
			reason = StringEscapeUtils.escapeJava(request.getParameter("reason"));
			
		    String utf8Reason = new UnicodeUnescaper().translate(reason);
		    reason = utf8Reason;
			
			userCodePayee = StringEscapeUtils.escapeJava(request.getParameter("userCodePayee"));
			CApayer = StringEscapeUtils.escapeJava(request.getParameter("CApayer"));
			CApayee = StringEscapeUtils.escapeJava(request.getParameter("CApayee"));
			
			if(CApayer == null || CApayer.isEmpty()) {
				errorMessage = "CAid payer was null, please contact the administrator";
				throw new IllegalArgumentException();
			}
			
			payer = caDao.getCAByCode(CApayer);

			if(payer == null) {
				errorMessage = "It was impossible to reach payer's current account information, please contact the administrator";
				throw new IllegalArgumentException();
			}
						
			if(reason == null || userCodePayee == null || CApayer == null || CApayee == null) {
				errorMessage = "You can't pass null strings";
				throw new IllegalArgumentException();
			} else if(amount <= 0) {
				errorMessage = "You can't do a transfer with an amount less than or equals to 0";
				throw new IllegalArgumentException();
			} else if(reason.isEmpty() || userCodePayee.isEmpty() || CApayer.isEmpty() || CApayee.isEmpty()) {
				errorMessage = "You can't pass empty strings";
				throw new IllegalArgumentException();
			} else if(userCodePayee.length() != 4 || CApayer.length() != 4 || CApayee.length() != 4){
				errorMessage = "A code length is incorrect";
				throw new IllegalArgumentException();
			} else if (CApayer.equals(CApayee)) {
				errorMessage = "Can't transfer an amuont from an account to the same account";
				throw new IllegalArgumentException();
			} else {
				try {
					Integer.parseInt(userCodePayee);
					Integer.parseInt(CApayer);
					Integer.parseInt(CApayee);
				} catch (NumberFormatException e) {
					errorMessage ="You passed codes that did not contain only numbers";
					throw new IllegalArgumentException();
				}
				
				UserDAO uDao = new UserDAO(connection);
				List<CurrentAccountBean> caPayeeList = new ArrayList<>();
				UserBean userPayee = null;
				
				try {
					userPayee = uDao.getUserByCode(userCodePayee);
				} catch (SQLException e) {
					e.printStackTrace();
					errorMessage = "Impossible to reach information about user with the submitted user code";
					throw new IllegalArgumentException();
				}
				
				if(userPayee == null) {
					errorMessage = "There was no user with that user code";
					throw new IllegalArgumentException();
				}
				
				caPayeeList = caDao.getCAByUser(userPayee.getIduser());
				payee = caDao.getCAByCode(CApayee);
				
				if(payer == null || payee == null) {
					errorMessage = "You inserted an invalid CA code";
					throw new IllegalArgumentException();
				} else if(payer.getTotal() < amount) {
					errorMessage = "Payer can't afford that amount of money";
					throw new IllegalArgumentException();
				} else if(!listContainsId(caPayeeList, payee)) {
					errorMessage = "There was no corrispondence between Current Account code and User code specified";
					throw new IllegalArgumentException();
				}
				inputIsOk = true;
			}
		} catch (IllegalArgumentException e) {
			//response.getWriter().println("You passed an argument considered illegal");
			if(errorMessage.equals("")) {
				errorMessage = "There was an error while connecting the server";
			}
			request.setAttribute("errorMessage", errorMessage);
			request.setAttribute("CAid", payer.getIdcurrentAccount());
			errorDispatcher.forward(request, response);			
			return;
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);

		}
		
		//transaction section
		//at this point I'm sure that input is coherent, now I have only to preserve DB integrity by Isolation (ACID)
		//and rollback in the cases in which there is some problem with DB (not due to input, but to connection)
		//https://dev.mysql.com/doc/refman/8.0/en/innodb-transaction-isolation-levels.html
		//MYSQL standard isolation is REPEATABLE_READ, it prevents dirty reads and non-repeatable reads, while allows
		//phantom reads
		//https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html		
		
		if(inputIsOk) {
			TransferDAO transferDao = new TransferDAO(connection);
			Savepoint savepoint = null;
			
			try {
				connection.setAutoCommit(false);
				savepoint = connection.setSavepoint();
				
			} catch (SQLException e) {
				e.printStackTrace();
				errorMessage = "There was an error while connecting the server";
				request.setAttribute("errorMessage", errorMessage);
				errorDispatcher.forward(request, response);
				//response.getWriter().println("There was an error while connecting to the server, retry later");
				//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "There was an error while connecting to the server, retry later");
				
			}
			
			try {
				float checkPayer = caDao.getTotalByCode(CApayer);
				float checkPayee = caDao.getTotalByCode(CApayee);
				boolean transPayer = caDao.updateCheckByAmount(CApayer, checkPayer - amount);
				boolean transPayee = caDao.updateCheckByAmount(CApayee, checkPayee + amount);
				boolean transRecord = transferDao.newTransfer(amount, reason, CApayer, CApayee);
				if(!transPayer || !transPayee || !transRecord) {
					throw new SQLException();
				} else {
					connection.commit();
					path = "/Pages/PaymentOK.jsp";
					response.setStatus(HttpServletResponse.SC_OK);
					request.setAttribute("CAid", payer.getIdcurrentAccount());
					RequestDispatcher dispatcher = request.getRequestDispatcher(path);
					dispatcher.forward(request, response);
					return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				errorMessage = "There was an error while connecting the server";
				request.setAttribute("errorMessage", errorMessage);
				errorDispatcher.forward(request, response);
				//throw new PaymentRefusedException("There was an error while connecting to the server, retry later");
				//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "There was an error while connecting to the server, retry later");
				try {
					connection.rollback(savepoint);
				} catch (SQLException e2) {
					e2.printStackTrace();
					errorMessage = "There was an error while connecting the server";
					request.setAttribute("errorMessage", errorMessage);
					errorDispatcher.forward(request, response);
					//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "There was an error while connecting to the server, retry later");
				}
			}
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
	
	private boolean listContainsId(List<CurrentAccountBean> list, CurrentAccountBean caBean) {
		for(CurrentAccountBean ca : list) {
			if(ca.getIdcurrentAccount() == caBean.getIdcurrentAccount()) {
				return true;
			}
		}
		return false;
	}

}
