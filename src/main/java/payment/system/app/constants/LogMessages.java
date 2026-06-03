package payment.system.app.constants;

public final class LogMessages {

    private LogMessages() {
    }

    public static final String PAYMENT_TRANSFER_INITIATED =
            "Payment transfer initiated: senderUserId={}, receiverUserId={}, amount={}";

    public static final String PAYMENT_TRANSFER_SUCCESS =
            "Payment transfer completed successfully: transactionRef={}";

    public static final String PAYMENT_TRANSFER_FAILED_LOG =
            "Payment transfer failed: transactionRef={}";

    public static final String TRANSACTION_CREATED =
            "Pending transaction created successfully: transactionId={}, transactionRef={}";

    public static final String TRANSACTION_STATUS_UPDATED =
            "Transaction status updated successfully: transactionId={}, status={}";

    public static final String SAME_USER_TRANSFER_ATTEMPT =
            "Invalid transfer attempt. Sender and receiver are same: userId={}";

    public static final String CALLING_WALLET_SERVICE =
            "Calling wallet service: senderUserId={}, receiverUserId={}, amount={}";

    public static final String WALLET_TRANSFER_SUCCESS =
            "Wallet transfer completed successfully: walletTransactionReference={}";

    public static final String WALLET_CLIENT_ERROR =
            "Wallet service returned client error: status={}";

    public static final String WALLET_SERVER_ERROR =
            "Wallet service returned server error: status={}";

    public static final String WALLET_COMMUNICATION_ERROR =
            "Wallet service communication error: statusCode={}, responseBody={}";

    public static final String UNEXPECTED_WALLET_EXCEPTION =
            "Unexpected error occurred while calling wallet service";

    public static final String TRANSACTION_CREATION_FAILED =
            "Failed to create pending transaction: transactionRef={}";

    public static final String TRANSACTION_STATUS_UPDATE_FAILED =
            "Failed to update transaction status: transactionId={}, status={}";
}