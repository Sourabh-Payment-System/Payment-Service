package payment.system.app.exception;

import payment.system.app.enums.ErrorCode;

public class InsufficientBalanceException
        extends BaseApplicationException {

    public InsufficientBalanceException(
            String message) {

        super(
                ErrorCode.INSUFFICIENT_BALANCE,
                message);
    }
}