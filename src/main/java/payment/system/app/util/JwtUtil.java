package payment.system.app.util;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import payment.system.app.config.JwtProperties;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {

        String secret = jwtProperties.getSecretBase64();

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT secret must be configured");
        }

        byte[] keyBytes = Decoders.BASE64.decode(secret);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT key must be at least 256 bits");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validates:
     * - signature
     * - expiration
     * - issuer
     * - audience
     *
     * Returns claims only when the JWT is valid.
     */
    public Claims validateToken(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(jwtProperties.getIssuer())
                .requireAudience(jwtProperties.getAudience())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}