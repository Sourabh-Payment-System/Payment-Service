package payment.system.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import payment.system.app.enums.PaymentStatus;

@Data
@Builder
public class WalletTransferResponse {

    private String walletTransactionReference;

    private Long senderUserId;

    private Long receiverUserId;

    private BigDecimal amount;

    private BigDecimal senderBalance;

    private BigDecimal receiverBalance;

    private PaymentStatus status;

    private LocalDateTime timestamp;
}