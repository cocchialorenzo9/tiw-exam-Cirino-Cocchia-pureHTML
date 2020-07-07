package enums;

import javax.servlet.http.HttpServletResponse;

public interface PaymentError extends HttpServletResponse {
	public static final int PAYMENT_ERROR = 7777;
}
