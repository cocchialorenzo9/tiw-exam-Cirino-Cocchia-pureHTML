package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import beans.CurrentAccountBean;
import beans.UserBean;
import daos.CurrentAccountDAO;

public class CoherenceSupervisor {
	
	public static boolean checkOwnsThisCurrentAccount(HttpServletRequest request, Connection connection, int idcurrentAccount) throws SQLException {
		UserBean user = (UserBean) request.getSession().getAttribute("user");
		CurrentAccountDAO caDao = new CurrentAccountDAO(connection);
		
		
		List<CurrentAccountBean> ownedByUser = caDao.getCAByUser(user.getIduser());
		
		for(CurrentAccountBean ca: ownedByUser) {
			if(ca.getIdcurrentAccount() == idcurrentAccount) {
				return true;
			}
		}
		
		return false;
	}

}
