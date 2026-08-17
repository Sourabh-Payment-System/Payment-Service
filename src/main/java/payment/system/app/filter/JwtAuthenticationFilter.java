package payment.system.app.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import payment.system.app.dto.ErrorResponse;
import payment.system.app.security.JwtAuthentication;
import payment.system.app.util.JwtUtil;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        return "/actuator/health"
                .equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        /*
         * No Authorization header.
         *
         * We don't immediately return 401 here.
         * Spring Security will later decide that the
         * endpoint requires authentication and invoke
         * CustomAuthenticationEntryPoint.
         */
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorization.substring(7).trim();

        if (token.isBlank()) {
            writeUnauthorized(
                    response,
                    "Authentication token is required");
            return;
        }

        try {

            Claims claims =
                    jwtUtil.validateToken(token);

            Long userId =
                    extractUserId(claims);

            String email =
                    claims.getSubject();

            if (email == null || email.isBlank()) {
                throw new JwtException(
                        "JWT subject is missing");
            }

            Collection<SimpleGrantedAuthority>
                    authorities =
                    extractAuthorities(claims);

            JwtAuthentication authentication =
                    new JwtAuthentication(
                            userId,
                            email,
                            authorities);

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication);

            filterChain.doFilter(
                    request,
                    response);

        } catch (JwtException
                 | IllegalArgumentException ex) {

            SecurityContextHolder.clearContext();

            writeUnauthorized(
                    response,
                    "Invalid or expired authentication token");
        }
    }

    private Long extractUserId(
            Claims claims) {

        Object value =
                claims.get("userId");

        if (value == null) {
            throw new IllegalArgumentException(
                    "userId claim is missing");
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(
                    value.toString());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Invalid userId claim");
        }
    }

    private Collection<SimpleGrantedAuthority>
    extractAuthorities(Claims claims) {

        Object value =
                claims.get("authorities");

        if (value == null) {
            return List.of();
        }

        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException(
                    "Invalid authorities claim");
        }

        return list.stream()
                .map(Object::toString)
                .filter(authority ->
                        !authority.isBlank())
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String message)
            throws IOException {

        ErrorResponse error =
                ErrorResponse.builder()
                        .timestamp(
                                java.time.LocalDateTime.now())
                        .status(
                                HttpServletResponse.SC_UNAUTHORIZED)
                        .error("Unauthorized")
                        .message(message)
                        .path(
                                response.getHeader(
                                        "X-Request-Path"))
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