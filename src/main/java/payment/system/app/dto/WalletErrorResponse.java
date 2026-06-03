package payment.system.app.dto;

import lombok.Data;

@Data
public class WalletErrorResponse {

    private Boolean success;

    private String message;
}