package com.isc.tokenservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SessionInfo {

    private String sessionId;

    private String customerId;

    private String deviceId;

    private String clientId;

    private String jwtId;

    private Instant createdAt;

    private String status;
}
