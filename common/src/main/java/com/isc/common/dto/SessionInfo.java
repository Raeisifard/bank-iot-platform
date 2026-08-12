package com.isc.common.dto;

import com.isc.common.enums.SessionReason;
import com.isc.common.enums.SessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Single session model shared by auth-time creation (token-service) and
 * connectivity updates (session-manager). Replaces the previous split
 * between the ClientSession record and the token-service SessionInfo dto.
 *
 * Auth-only fields (refreshTokenId, createdAt, expireAt) are set once at
 * login and never touched by connection events. Connectivity fields
 * (status, node, protocol, ipAddress, lastEventTimestamp) are set/refreshed
 * by CLIENT_CONNECTED / CLIENT_DISCONNECTED / CLIENT_KEEPALIVE events.
 */
@Getter
@Builder(toBuilder = true)
public class SessionInfo {

    private String sessionId;   // = jwt.sid, primary Redis key
    private String customerId;
    private String deviceId;
    private String clientId;
    private String username;
    private String ipAddress;
    private String node;
    private Integer protocol;

    private String refreshTokenId;
    private Instant createdAt;
    private Instant expireAt;
    private Instant lastRefreshAt;
    private Long lastEventTimestamp;

    private SessionStatus status;
    private SessionReason reason;
}
