package payment.system.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import payment.system.app.enums.PaymentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchRequest {

    private Long senderUserId;

    private Long receiverUserId;

    private PaymentStatus status;

    @DecimalMin(value = "0.0")
    private BigDecimal minAmount;

    @DecimalMin(value = "0.0")
    private BigDecimal maxAmount;

    private LocalDateTime from;

    private LocalDateTime to;

    @Pattern(
    	    regexp="^[A-Za-z0-9-]*$"
    	)
    private String transactionReference;
}