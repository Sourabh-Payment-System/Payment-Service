package payment.system.app.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddMoneyRequest {

    private Long userId;

    private BigDecimal amount;
}