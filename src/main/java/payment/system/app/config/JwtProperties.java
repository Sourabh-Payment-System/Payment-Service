package payment.system.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "jwt")
@Data
@Component
public class JwtProperties {

    private String secretBase64;
    private String issuer;
    private String audience;
}