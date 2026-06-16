package payment.system.app.exception;

import payment.system.app.enums.ErrorCode;

public class IdempotencyRecordNotFoundException
        extends BaseApplicationException {

    public IdempotencyRecordNotFoundException(
            String idempotencyKey) {

        super(
                ErrorCode.IDEMPOTENCY_RECORD_NOT_FOUND,
                "Idempotency record not found for key: "
                        + idempotencyKey);
    }
}