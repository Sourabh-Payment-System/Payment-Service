package payment.system.app.exception;

import payment.system.app.enums.ErrorCode;

public class PaymentFailedException
        extends BaseApplicationException {

    public PaymentFailedException(
            String message) {

        super(
                ErrorCode.PAYMENT_FAILED,
                message);
    }

    public PaymentFailedException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.PAYMENT_FAILED,
                message,
                cause);
    }
}