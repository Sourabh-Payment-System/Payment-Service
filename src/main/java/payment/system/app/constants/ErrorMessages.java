package payment.system.app.constants;

public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static final String PAYMENT_TRANSFER_FAILED_MESSAGE =
            "Payment transfer failed";

    public static final String WALLET_TRANSFER_FAILED =
            "Wallet transfer failed";

    public static final String WALLET_RESPONSE_NULL =
            "Wallet response is null";

    public static final String WALLET_RESPONSE_NULL_MESSAGE =
            "Wallet service returned null response";

    public static final String WALLET_REFERENCE_MISSING =
            "Wallet transaction reference missing";

    public static final String WALLET_STATUS_MISSING =
            "Wallet transaction status missing";

    public static final String INVALID_WALLET_REQUEST =
            "Invalid wallet transfer request";

    public static final String WALLET_SERVICE_UNAVAILABLE =
            "Wallet service temporarily unavailable";

    public static final String WALLET_SERVICE_COMMUNICATION_FAILED =
            "Wallet service communication failed";

    public static final String UNEXPECTED_WALLET_ERROR =
            "Unexpected wallet service error";

    public static final String SENDER_RECEIVER_SAME =
            "Sender and receiver cannot be same";

    public static final String SENDER_USERID_MISMATCH =
            "Sender userId mismatch";

    public static final String RECEIVER_USERID_MISMATCH =
            "Receiver userId mismatch";

    public static final String TRANSFER_AMOUNT_MISMATCH =
            "Transfer amount mismatch";

    public static final String TRANSACTION_NOT_FOUND =
            "Transaction not found with id: ";

    public static final String UNABLE_TO_CREATE_TRANSACTION =
            "Unable to create payment transaction";

    public static final String UNABLE_TO_UPDATE_TRANSACTION =
            "Unable to update transaction status";
}