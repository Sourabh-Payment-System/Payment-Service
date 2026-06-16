package payment.system.app.exception;

import payment.system.app.enums.ErrorCode;

public class ResourceNotFoundException
        extends BaseApplicationException {

    public ResourceNotFoundException(
            String message) {

        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                message);
    }
}