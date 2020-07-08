package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.TransferBean;

public class TransferDAO {
	
	private Connection con;

	public TransferDAO(Connection connection) {
		this.con = connection;
	}
	
	public List<TransferBean> getTransfersByCAId (String CAcode) throws SQLException {
		String query = "SELECT * FROM transfer WHERE CApayer = ? OR CApayee = ? ORDER BY date DESC";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			System.out.println("id passed: " + CAcode);
			pstatement.setString(1, CAcode);
			pstatement.setString(2, CAcode);
			ResultSet result = pstatement.executeQuery();
			List<TransferBean> returningList = new ArrayList<>();
			while(result.next()) {
				TransferBean newTransfer = new TransferBean();
				newTransfer.setIdtransfer(result.getInt("idtransfer"));
				newTransfer.setAmount(result.getInt("amount"));
				newTransfer.setDate(result.getDate("date"));
				newTransfer.setReason(result.getString("reason"));
				newTransfer.setCApayer(result.getString("CApayer"));
				newTransfer.setCApayee(result.getString("CApayee"));
				returningList.add(newTransfer);
			}
			return returningList;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		}
	}
	
	public boolean newTransfer(float amount, String reason, String CApayer, String CApayee) throws SQLException{
		String query = "INSERT INTO transfer (amount, reason, CApayer, CApayee) VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setFloat(1, amount);
			pstatement.setString(2,  reason);
			pstatement.setString(3,  CApayer);
			pstatement.setString(4,  CApayee);
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

}
