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
				newCA.setTotal(result.getFloat("total"));
				newCA.setCAcode(result.getString("CAcode"));
			} else {
				newCA = null;
			}
			return newCA;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public CurrentAccountBean getCAByCode(String CAcode) {
		String query = "SELECT * FROM currentAccount WHERE CAcode = ?";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setString(1, CAcode);
			ResultSet result = pstatement.executeQuery();
			CurrentAccountBean newCA = new CurrentAccountBean();
			if(result.next()) {
				newCA.setIdcurrentAccount(result.getInt("idcurrentAccount"));
				newCA.setTotal(result.getFloat("total"));
				newCA.setCAcode(result.getString("CAcode"));
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
				newCA.setTotal(result.getFloat("total"));
				newCA.setCAcode(result.getString("CAcode"));
				returningList.add(newCA);
			}
			return returningList;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public float getTotalByCode(String CA) throws SQLException {
		String query = "SELECT * FROM currentAccount WHERE CAcode = ? ";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setString(1, CA);
			ResultSet result = pstatement.executeQuery();
			if(result.next()) {
				return result.getFloat("total");
			} else {
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		}
	}
	
	public boolean updateCheckByAmount(String CA, float amount) throws SQLException {
		String query = "UPDATE currentAccount SET total = ? WHERE CAcode = ? ";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setFloat(1, amount);
			pstatement.setString(2, CA);
			int flag = pstatement.executeUpdate();
			if(flag == 0) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		}
	}
	
	/*
	public boolean updateCheckByAmountInt(int CA, int amount) throws SQLException {
		String query = "UPDATE currentAccount SET check = ? WHERE idcurrentAccount = ? ";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setInt(1, amount);
			pstatement.setInt(2, CA);
			int flag = pstatement.executeUpdate();
			if(flag == 0) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		}
	}
	*/

}
