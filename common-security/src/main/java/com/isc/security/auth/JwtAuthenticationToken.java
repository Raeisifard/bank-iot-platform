package com.isc.security.auth;

import com.isc.security.model.JwtClaims;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final JwtClaims claims;

    public JwtAuthenticationToken(JwtClaims claims) {

        super(Collections.emptyList());
        this.claims = claims;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return claims;
    }

    @Override
    public Object getPrincipal() {
        return claims.getCa();
    }
}
