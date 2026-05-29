package payment.system.app.exception;

public class PaymentProcessingException
        extends RuntimeException {

    public PaymentProcessingException(
            String message) {

        super(message);
    }
}