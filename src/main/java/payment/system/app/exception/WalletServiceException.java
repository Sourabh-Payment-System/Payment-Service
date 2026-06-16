package payment.system.app.exception;

import lombok.Getter;
import payment.system.app.enums.ErrorCode;

@Getter
public class WalletServiceException
        extends BaseApplicationException {

    private final int statusCode;

    private final String errorResponse;

    public WalletServiceException(
            String message,
            int statusCode,
            String errorResponse) {

        super(
                ErrorCode.WALLET_SERVICE_ERROR,
                message);

        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }
}