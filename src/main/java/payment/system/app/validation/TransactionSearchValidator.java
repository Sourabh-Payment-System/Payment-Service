package payment.system.app.validation;

import payment.system.app.dto.TransactionSearchRequest;
import payment.system.app.exception.BadRequestException;

public final class TransactionSearchValidator {

    private TransactionSearchValidator() {
    }

    public static void validate(
            TransactionSearchRequest request) {

        if (request == null) {
            return;
        }

        if (request.getMinAmount() != null
                && request.getMaxAmount() != null
                && request.getMinAmount()
                .compareTo(request.getMaxAmount()) > 0) {

            throw new BadRequestException(
                    "Minimum amount cannot be greater than maximum amount");
        }

        if (request.getFrom() != null
                && request.getTo() != null
                && request.getFrom().isAfter(request.getTo())) {

            throw new BadRequestException(
                    "From date cannot be after To date");
        }
    }
}