package com.isc.common.dto;

import com.isc.common.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSession {

    /**
     * Session ID
     */
    private String sid;

    /**
     * JWT ID
     */
    private String jti;

    /**
     * Refresh Token ID
     */
    private String refreshTokenId;

    /**
     * Customer ID (EMQX client_id)
     */
    private String clientId;

    /**
     * Username
     */
    private String username;

    /**
     * Device ID
     */
    private String deviceId;

    /**
     * Application / Channel ID
     */
    private String channelId;

    /**
     * JWT Issuer
     */
    private String issuer;

    /**
     * JWT Audience
     */
    private String audience;

    /**
     * MQTT Protocol Version
     */
    private Integer protocol;

    /**
     * Client IP Address
     */
    private String ipAddress;

    /**
     * ONLINE, OFFLINE, LOGGED_OUT, KICKED
     */
    private SessionStatus status;

    /**
     * EMQX Connect Timestamp
     */
    private Long connectedAt;

    /**
     * Last Activity Timestamp
     */
    private Long lastSeenAt;

    /**
     * Disconnect Timestamp
     */
    private Long disconnectedAt;

    /**
     * JWT Expiration Epoch Millis
     */
    private Long expiresAt;

    /**
     * Server/Pod/Node Handling This Session
     *
     * Examples:
     * auth-node-01
     * mqtt-node-05
     * pod-session-service-7f4f8
     */
    private String serverNodeId;
}
