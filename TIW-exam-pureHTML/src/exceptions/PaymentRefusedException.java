package exceptions;

public class PaymentRefusedException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public PaymentRefusedException(String message) {
		super(message);
	}
	
}
