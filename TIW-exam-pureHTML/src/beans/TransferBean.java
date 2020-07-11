package beans;

import java.util.Date;

public class TransferBean {
	private int idtransfer;
	private Date date;
	private float amount;
	private String reason;
	private String CApayer;
	private String CApayee;
	public int getIdtransfer() {
		return idtransfer;
	}
	public void setIdtransfer(int idtransfer) {
		this.idtransfer = idtransfer;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getCApayer() {
		return CApayer;
	}
	public void setCApayer(String cApayer) {
		CApayer = cApayer;
	}
	public String getCApayee() {
		return CApayee;
	}
	public void setCApayee(String cApayee) {
		CApayee = cApayee;
	}

}
