package com.isc.tokenservice.dto;

import com.isc.tokenservice.enums.RefreshTokenStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class RefreshTokenInfo {

    //private String refreshTokenId;

    private String sessionId;

    private String customerId;

    private String deviceId;

    private String clientId;

    private String refreshTokenHash;

    private Instant createdAt;

    private Instant lastRotatedAt;

    private Instant expireAt;

    private RefreshTokenStatus status;

    private Instant revokedAt;
}
