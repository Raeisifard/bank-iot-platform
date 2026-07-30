package com.isc.tokenservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuthTokens {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Instant accessTokenExpiresAt;

    private Instant refreshTokenExpiresAt;
}
