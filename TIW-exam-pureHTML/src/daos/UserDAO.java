package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import beans.UserBean;

public class UserDAO {
	private Connection con;

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	public UserBean checkCredentials(String usrcode, String pwd) throws SQLException {
		String query = "SELECT * FROM user WHERE usercode = ? AND password =?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, usrcode);
			pstatement.setString(2, pwd);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					UserBean user = new UserBean();
					user.setIduser(result.getInt("iduser"));
					user.setName(result.getString("name"));
					user.setUsercode(result.getString("usercode"));
					return user;
				}
			}
		}
	}
}
