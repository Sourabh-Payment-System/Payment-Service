package payment.system.app.exception;

import lombok.Getter;

@Getter
public class WalletServiceException
        extends RuntimeException {

    private final int statusCode;

    private final String errorResponse;

    public WalletServiceException(
            String message,
            int statusCode,
            String errorResponse) {

        super(message);

        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }
}