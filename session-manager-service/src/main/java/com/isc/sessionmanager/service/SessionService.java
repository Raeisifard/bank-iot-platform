package com.isc.sessionmanager.service;

import com.isc.common.dto.SessionInfo;
import com.isc.common.enums.SessionReason;
import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.contract.event.session.ClientDisconnectedEvent;

/**
 * Single source of truth for session state in Redis. Used by:
 *  - token-service (via gRPC), at authentication time (create/validate/refresh/revoke)
 *  - session-manager, for MQTT connection lifecycle events (Kafka)
 *
 * Previously only handleConnectionEvent/handleDisconnectionEvent were declared
 * here, forcing GrpcSessionService to depend on the concrete SessionServiceImpl
 * instead of this interface. Widened to the full contract so callers can be
 * tested against a mock/fake implementation.
 */
public interface SessionService {

    // ---------------------------------------------------------------
    // Authentication-time lifecycle
    // ---------------------------------------------------------------

    String create(SessionInfo session);

    SessionInfo getSession(String sessionId);

    boolean isValid(String sessionId);

    void revokeSession(String sessionId, SessionReason reason);

    void deleteSession(String sessionId);

    void refreshSession(String sessionId);

    void touchSession(String sessionId);

    String getDeviceSession(String customerId, String deviceId);

    boolean hasActiveDeviceSession(String customerId, String deviceId);

    String getClientSession(String clientId);

    // ---------------------------------------------------------------
    // Connection lifecycle handling (Kafka)
    // ---------------------------------------------------------------

    /**
     * Applies a connection lifecycle event (CONNECTED / DISCONNECTED / KEEPALIVE)
     * to the session store, creating or updating the Redis record as needed.
     *
     * Implementations must be idempotent and tolerant of out-of-order delivery
     * (e.g. a DISCONNECTED for an old sub-session arriving after a newer CONNECTED).
     */
    void handleConnectionEvent(ClientConnectedEvent event);

    void handleDisconnectionEvent(ClientDisconnectedEvent event);
}
