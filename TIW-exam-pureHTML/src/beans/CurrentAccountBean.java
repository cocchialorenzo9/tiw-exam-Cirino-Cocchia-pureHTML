package beans;

public class CurrentAccountBean {
	private int idcurrentAccount;
	private String CAcode;
	private float total;
	public int getIdcurrentAccount() {
		return idcurrentAccount;
	}
	public void setIdcurrentAccount(int idcurrentAccount) {
		this.idcurrentAccount = idcurrentAccount;
	}
	
	public String getCAcode() {
		return CAcode;
	}
	public void setCAcode(String cAcode) {
		CAcode = cAcode;
	}
	public float getTotal() {
		return total;
	}
	public void setTotal(float total) {
		this.total = total;
	}

}
