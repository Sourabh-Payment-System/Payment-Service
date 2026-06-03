package payment.system.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
@EnableRetry
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}