package payment.system.app.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import payment.system.app.dto.ErrorResponse;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        ErrorResponse error =
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(
                                HttpServletResponse.SC_UNAUTHORIZED)
                        .error("Unauthorized")
                        .message(
                                "Authentication is required to access this resource")
                        .path(
                                request.getRequestURI())
                        .method(
                                request.getMethod())
                        .build();

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(
                response.getWriter(),
                error);
    }
}