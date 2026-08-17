package payment.system.app.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import payment.system.app.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    private final CustomAuthenticationEntryPoint
            authenticationEntryPoint;

    private final CustomAccessDeniedHandler
            accessDeniedHandler;

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        return http
                .csrf(csrf -> csrf.disable())

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(
                                authenticationEntryPoint)
                          .accessDeniedHandler(
                                accessDeniedHandler))

                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(
                                        "/actuator/health")
                                .permitAll()

                                .anyRequest()
                                .authenticated())

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}