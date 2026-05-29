package payment.system.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {

    private String transactionReference;

    private Long senderUserId;

    private Long receiverUserId;

    private BigDecimal amount;

    private String status;

    private LocalDateTime timestamp;
}