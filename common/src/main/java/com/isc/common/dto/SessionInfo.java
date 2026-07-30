package com.isc.common.dto;

import com.isc.common.enums.SessionReason;
import com.isc.common.enums.SessionStatus;
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

    private String refreshTokenId;

    private Instant createdAt;

    private Instant expireAt;

    private Instant lastRefreshAt;

    @Builder.Default
    private SessionStatus status = SessionStatus.ONLINE;

    @Builder.Default
    private SessionReason reason = SessionReason.NONE;
}
