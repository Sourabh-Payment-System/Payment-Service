package payment.system.app.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Data;

@Data
public class TransferRequest {

    @NotNull(message = "Sender userId is required")
    @Positive(message = "Sender userId must be positive")
    private Long senderUserId;

    @NotNull(message = "Receiver userId is required")
    @Positive(message = "Receiver userId must be positive")
    private Long receiverUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "1.0",
            inclusive = true,
            message = "Amount must be greater than 0")
    private BigDecimal amount;
}