package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.CurrentAccountBean;

public class CurrentAccountDAO {
	private Connection con;

	public CurrentAccountDAO(Connection connection) {
		this.con = connection;
	}

	public CurrentAccountBean getCAById(int idcurrentAccount) {
		String query = "SELECT * FROM currentAccount WHERE idcurrentAccount = ?";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idcurrentAccount);
			ResultSet result = pstatement.executeQuery();
			CurrentAccountBean newCA = new CurrentAccountBean();
			if(result.next()) {
				newCA.setIdcurrentAccount(result.getInt("idcurrentAccount"));
				newCA.setCheck(result.getInt("check"));
			} else {
				newCA = null;
			}
			return newCA;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<CurrentAccountBean> getCAByUser(int iduser){
		String query = "SELECT * FROM currentAccount WHERE iduser = ?";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setInt(1, iduser);
			ResultSet result = pstatement.executeQuery();
			List<CurrentAccountBean> returningList = new ArrayList<>();
			while(result.next()) {
				CurrentAccountBean newCA = new CurrentAccountBean();
				newCA.setIdcurrentAccount(result.getInt("idcurrentAccount"));
				newCA.setCAcode(result.getString("CAcode"));
				newCA.setCheck(result.getInt("check"));
				returningList.add(newCA);
			}
			return returningList;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
