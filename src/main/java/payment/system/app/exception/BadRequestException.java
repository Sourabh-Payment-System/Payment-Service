package payment.system.app.exception;


import payment.system.app.enums.ErrorCode;
public class BadRequestException
        extends BaseApplicationException {

    public BadRequestException(
            String message) {

        super(
                ErrorCode.BAD_REQUEST,
                message);
    }
}