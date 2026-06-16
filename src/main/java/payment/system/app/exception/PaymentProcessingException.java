package payment.system.app.exception;

import payment.system.app.enums.ErrorCode;

public class PaymentProcessingException
        extends BaseApplicationException {

    public PaymentProcessingException(
            String message) {

        super(
                ErrorCode.PAYMENT_PROCESSING_ERROR,
                message);
    }

    public PaymentProcessingException(
            ErrorCode errorCode,
            String message) {

        super(
                errorCode,
                message);
    }

    public PaymentProcessingException(
            ErrorCode errorCode,
            String message,
            Throwable cause) {

        super(
                errorCode,
                message,
                cause);
    }
}