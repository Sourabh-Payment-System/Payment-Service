package payment.system.app.config;

import jakarta.validation.constraints.NotBlank;

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
}