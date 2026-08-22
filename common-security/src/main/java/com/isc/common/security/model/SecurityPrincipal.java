package com.isc.common.security.model;

import com.isc.common.dto.ClientAttributes;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class SecurityPrincipal {
    private final String subject;
    private final Set<String> authorities;
    private final Map<String, Object> claims;
    private final ClientAttributes clientAttributes;

    public SecurityPrincipal(String subject, Set<String> authorities,
                             Map<String, Object> claims, ClientAttributes clientAttributes) {
        this.subject = subject;
        this.authorities = authorities == null ? Set.of() : Collections.unmodifiableSet(authorities);
        this.claims = claims == null ? Map.of() : Collections.unmodifiableMap(claims);
        this.clientAttributes = clientAttributes;
    }

    public String getSubject() {
        return subject;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    public ClientAttributes getClientAttributes() {
        return clientAttributes;
    }

    public String getName() {
        return subject;
    }
}
