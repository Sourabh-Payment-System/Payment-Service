package payment.system.app.constants;

public final class TransactionConstants {

    private TransactionConstants() {
    }

    public static final String TRANSACTION_PREFIX = "TXN-";

    public static final int TRANSACTION_REFERENCE_LENGTH = 12;

    public static final String SUCCESS_STATUS = "SUCCESS";
    
    public static final long PROCESSING_TIMEOUT_MINUTES = 5;
}