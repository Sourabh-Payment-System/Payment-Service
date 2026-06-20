package payment.system.app.utility;

import java.util.UUID;

import org.springframework.stereotype.Component;
import static payment.system.app.constants.TransactionConstants.TRANSACTION_PREFIX;

@Component
public class ReferenceGenerator {
	private static final int UUID_LENGTH = 32;

    public String generateReference(int length) {

        String value = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase();

        

        if (length <= 0 || length > UUID_LENGTH) {
            throw new IllegalArgumentException(
                    "Length must be between 1 and " + UUID_LENGTH);
        }

        return value.substring(0, length);
    }
}
