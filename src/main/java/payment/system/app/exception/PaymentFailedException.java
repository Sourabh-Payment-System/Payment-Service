package payment.system.app.exception;

public class PaymentFailedException
        extends RuntimeException {

    public PaymentFailedException(
            String message) {

        super(message);
    }

    public PaymentFailedException(
            String message,
            Throwable ex) {

        super(message, ex);
    }
}