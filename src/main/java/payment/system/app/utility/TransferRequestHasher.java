package payment.system.app.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

import payment.system.app.dto.TransferRequest;

@Component
public class TransferRequestHasher {

    public String generateHash(
            TransferRequest request) {

        try {

        	String payload =
        	        String.valueOf(
        	                request.getSenderUserId())
        	        + "|"
        	        + String.valueOf(
        	                request.getReceiverUserId())
        	        + "|"
        	        + String.valueOf(
        	                request.getAmount());

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            payload.getBytes(
                                    StandardCharsets.UTF_8));

            StringBuilder builder =
                    new StringBuilder();

            for (byte b : hash) {

                builder.append(
                        String.format("%02x", b));
            }

            return builder.toString();

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "Unable to generate request hash",
                    ex);
        }
    }
}