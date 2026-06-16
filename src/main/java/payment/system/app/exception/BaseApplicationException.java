package payment.system.app.exception;

import lombok.Getter;
import payment.system.app.enums.ErrorCode;

@Getter
public abstract class BaseApplicationException
        extends RuntimeException {

    private final ErrorCode errorCode;

    protected BaseApplicationException(
            ErrorCode errorCode,
            String message) {

        super(message);
        this.errorCode = errorCode;
    }

    protected BaseApplicationException(
            ErrorCode errorCode,
            String message,
            Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }
}