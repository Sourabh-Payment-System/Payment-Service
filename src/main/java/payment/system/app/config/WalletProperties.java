package payment.system.app.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "wallet.service")
public class WalletProperties {

    @NotBlank
    private String baseUrl;
    @Positive
    private Integer connectTimeout;
    @Positive
    private Integer readTimeout;
}