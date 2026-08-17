package payment.system.app.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthentication
        extends AbstractAuthenticationToken {

    private final Long userId;
    private final String email;

    public JwtAuthentication(
            Long userId,
            String email,
            Collection<? extends GrantedAuthority> authorities) {

        super(authorities);

        this.userId = userId;
        this.email = email;

        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}