package payment.system.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
	private final WalletProperties walletProperties;

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
	            .build();
	}
}