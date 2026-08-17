package payment.system.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final WalletProperties walletProperties;

    private final JwtPropagationInterceptor
            jwtPropagationInterceptor;

    @Bean
    public RestClient restClient() {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(
                walletProperties.getConnectTimeout());

        factory.setReadTimeout(
                walletProperties.getReadTimeout());

        return RestClient.builder()
                .requestFactory(factory)
                .requestInterceptor(
                        jwtPropagationInterceptor)
                .build();
    }
}