package com.isc.security.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class JwtClaims {

    private String sessionId;
    private String clientId;
    private String userId;
    private String username;
    private List<String> roles;
    private Instant issuedAt;
    private Instant expiration;
}