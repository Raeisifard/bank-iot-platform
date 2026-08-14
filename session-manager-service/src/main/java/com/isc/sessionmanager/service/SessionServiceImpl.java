package com.isc.sessionmanager.service;

import com.isc.common.dto.ClientAttributes;
import com.isc.common.dto.SessionInfo;
import com.isc.common.enums.SessionReason;
import com.isc.common.enums.SessionStatus;
import com.isc.common.exception.SessionNotFoundException;
import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.contract.event.session.ClientDisconnectedEvent;
import com.isc.sessionmanager.config.SessionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.isc.common.constants.ClientSessionFieldsName.*;
import static com.isc.common.constants.RedisKeys.*;

/**
 * Single source of truth for session state in Redis. Used by:
 *  - token-service, at authentication time (create/validate/refresh/revoke)
 *  - session-manager, for MQTT connection lifecycle events (handleConnectionEvent)
 *
 * Storage: one Redis hash per session under key "session:<sessionId>",
 * where sessionId is always jwt.sid. Secondary lookup keys (device -> sid,
 * mqtt clientId -> sid) point back to the same session.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final StringRedisTemplate redis;
    private final SessionProperties sessionProperties;

    // ---------------------------------------------------------------
    // Authentication-time lifecycle (formerly tokenservice.identity.SessionService)
    // ---------------------------------------------------------------

    @Override
    public String create(SessionInfo session) {
        String key = buildSessionKey(session.getSessionId());

        Map<String, String> values = new HashMap<>();
        values.put(SESSION_ID, session.getSessionId());
        values.put(CUSTOMER_ID, session.getCustomerId());
        values.put(DEVICE_ID, session.getDeviceId());
        values.put(CLIENT_ID, session.getClientId());
        values.put(REFRESH_TOKEN_ID, session.getRefreshTokenId());
        values.put(CREATED_AT, session.getCreatedAt().toString());
        values.put(EXPIRE_AT, session.getExpireAt().toString());
        values.put(LAST_REFRESH_AT, session.getCreatedAt().toString());
        values.put(STATUS, (session.getStatus() != null ? session.getStatus() : SessionStatus.ONLINE).name());
        if (session.getReason() != null) {
            values.put(REASON, session.getReason().name());
        }

        redis.opsForHash().putAll(key, values);

        Duration ttl = sessionProperties.getSessionIdleTtl().plus(sessionProperties.getSessionAuditTtl());
        redis.expire(key, ttl);

        redis.opsForValue().set(buildDeviceSessionKey(session.getCustomerId(), session.getDeviceId()),
                session.getSessionId(), ttl);
        redis.opsForValue().set(buildClientSessionKey(session.getClientId()),
                session.getSessionId(), ttl);

        return session.getSessionId();
    }

    @Override
    public SessionInfo getSession(String sessionId) {
        Map<Object, Object> map = redis.opsForHash().entries(buildSessionKey(sessionId));
        if (map.isEmpty()) {
            return null;
        }
        return toSessionInfo(map);
    }

    @Override
    public boolean isValid(String sessionId) {
        SessionInfo session = getSession(sessionId);
        if (session == null || session.getStatus() != SessionStatus.ONLINE) {
            return false;
        }
        if (session.getExpireAt() != null && session.getExpireAt().isBefore(Instant.now())) {
            return false;
        }
        return session.getLastRefreshAt() == null
                || !session.getLastRefreshAt().plus(sessionProperties.getSessionIdleTtl()).isBefore(Instant.now());
    }

    @Override
    public void revokeSession(String sessionId, SessionReason reason) {
        String key = buildSessionKey(sessionId);
        SessionInfo session = requireSession(sessionId, key);
        if (session.getStatus() == SessionStatus.REVOKED) {
            return;
        }
        redis.opsForHash().put(key, STATUS, SessionStatus.REVOKED.name());
        redis.opsForHash().put(key, REASON, (reason != null ? reason : SessionReason.NONE).name());
        redis.delete(buildDeviceSessionKey(session.getCustomerId(), session.getDeviceId()));
        redis.delete(buildClientSessionKey(session.getClientId()));
    }

    @Override
    public void deleteSession(String sessionId) {
        String key = buildSessionKey(sessionId);
        SessionInfo session = requireSession(sessionId, key);
        redis.delete(key);
        redis.delete(buildDeviceSessionKey(session.getCustomerId(), session.getDeviceId()));
        redis.delete(buildClientSessionKey(session.getClientId()));
    }

    @Override
    public void refreshSession(String sessionId) {
        String key = buildSessionKey(sessionId);
        requireSession(sessionId, key);
        redis.opsForHash().put(key, LAST_REFRESH_AT, Instant.now().toString());
        redis.expire(key, sessionProperties.getSessionIdleTtl());
    }

    @Override
    public void touchSession(String sessionId) {
        String key = buildSessionKey(sessionId);
        if (!redis.hasKey(key)) {
            throw new SessionNotFoundException(key + " does not exist!");
        }
        redis.opsForHash().put(key, LAST_REFRESH_AT, Instant.now().toString());
    }

    @Override
    public String getDeviceSession(String customerId, String deviceId) {
        return redis.opsForValue().get(buildDeviceSessionKey(customerId, deviceId));
    }

    @Override
    public boolean hasActiveDeviceSession(String customerId, String deviceId) {
        String sessionId = getDeviceSession(customerId, deviceId);
        return sessionId != null && isValid(sessionId);
    }

    @Override
    public String getClientSession(String clientId) {
        return redis.opsForValue().get(buildClientSessionKey(clientId));
    }

    // ---------------------------------------------------------------
    // Connection lifecycle handling (formerly sessionmanager.service.SessionServiceImpl)
    // ---------------------------------------------------------------

    /**
     * Applies a CLIENT_CONNECTED / CLIENT_DISCONNECTED / CLIENT_KEEPALIVE
     * event to the session identified by jwt.sid.
     */
    @Override
    public void handleConnectionEvent(ClientConnectedEvent event) {
        ClientAttributes jwt = event.getJwt();
        String sid = jwt.getSid();
        String key = buildSessionKey(sid);

        Map<Object, Object> existing = redis.opsForHash().entries(key);

        if (isStale(event, existing)) {
            log.info("Discarding stale/out-of-order event: sid={} eventType={} eventTs={} currentTs={}",
                    sid, event.getEventType(), event.getConnectedAt(), existing.get(LAST_EVENT_TIMESTAMP));
            return;
        }

        // CLIENT_DISCONNECTED never reaches this method: ConnectionEventListener
        // dispatches by Java type (ClientConnectedEvent vs ClientDisconnectedEvent),
        // and disconnects are routed to handleDisconnectionEvent() instead.
        SessionStatus status = switch (event.getEventType()) {
            case CLIENT_CONNECTED, CLIENT_KEEPALIVE -> SessionStatus.ONLINE;
            default -> null;
        };

        if (status == null) {
            log.warn("Unhandled event type for connectivity update: sid={} eventType={}", sid, event.getEventType());
            return;
        }

        applyConnectivity(sid, key, event, status, existing.isEmpty());
    }

    @Override
    public void handleDisconnectionEvent(ClientDisconnectedEvent event) {
        ClientAttributes jwt = event.getJwt();
        String sid = jwt.getSid();
        String key = buildSessionKey(sid);

        Map<Object, Object> existing = redis.opsForHash().entries(key);

        // Session does not exist.
        // A disconnect event must never create a new session.
        if (existing.isEmpty()) {
            log.debug(
                    "Ignoring CLIENT_DISCONNECTED for non-existing session: sid={} clientId={}",
                    sid,
                    jwt.getCid()
            );
            return;
        }

        // Ignore old / out-of-order disconnect events.
        Object lastTs = existing.get(LAST_EVENT_TIMESTAMP);

        if (lastTs != null
                && event.getDisconnectedAt() < Long.parseLong((String) lastTs)) {

            log.info(
                    "Discarding stale/out-of-order disconnect event: sid={} eventTs={} currentTs={}",
                    sid,
                    event.getDisconnectedAt(),
                    lastTs
            );

            return;
        }

        Map<String, String> fields = new HashMap<>();

        fields.put(SESSION_ID, sid);
        fields.put(STATUS, SessionStatus.OFFLINE.name());
        fields.put(LAST_EVENT_TIMESTAMP, String.valueOf(event.getDisconnectedAt()));
        fields.put(UPDATED_AT, Instant.now().toString());

        /*
         * Connection information may be present in the disconnect event.
         * Update it when available, otherwise keep the existing Redis values.
         */
        if (jwt.getDid() != null) {
            fields.put(DEVICE_ID, jwt.getDid());
        }

        if (jwt.getCid() != null) {
            fields.put(CLIENT_ID, jwt.getCid());
        }

        if (event.getUsername() != null) {
            fields.put(USERNAME, event.getUsername());
        }

        if (event.getIpAddress() != null) {
            fields.put(IP_ADDRESS, event.getIpAddress());
        }

        if (event.getNodeId() != null) {
            fields.put(NODE, event.getNodeId());
        }

        if (event.getProtocol() != null) {
            fields.put(PROTOCOL, String.valueOf(event.getProtocol()));
        }

        redis.opsForHash().putAll(key, fields);

        log.info(
                "Session connectivity updated: sid={} status=OFFLINE node={}",
                sid,
                event.getNodeId()
        );
    }

    /** Guards against a redelivered/out-of-order event overwriting a newer state. */
    private boolean isStale(ClientConnectedEvent event, Map<Object, Object> existing) {
        Object lastTs = existing.get(LAST_EVENT_TIMESTAMP);
        return lastTs != null && event.getConnectedAt() < Long.parseLong((String) lastTs);
    }

    private void applyConnectivity(String sid, String key, ClientConnectedEvent event,
                                   SessionStatus status, boolean isNewRecord) {
        ClientAttributes jwt = event.getJwt();

        Map<String, String> fields = new HashMap<>();
        fields.put(SESSION_ID, sid);
        fields.put(STATUS, status.name());
        fields.put(DEVICE_ID, jwt.getDid());
        fields.put(CLIENT_ID, jwt.getCid());
        fields.put(USERNAME, event.getUsername());
        fields.put(IP_ADDRESS, event.getIpAddress());
        fields.put(NODE, event.getNodeId());
        fields.put(PROTOCOL, String.valueOf(event.getProtocol()));
        fields.put(LAST_EVENT_TIMESTAMP, String.valueOf(event.getConnectedAt()));
        fields.put(UPDATED_AT, Instant.now().toString());

        if (isNewRecord) {
            // No authenticated session found for this sid (edge case: expired/missed
            // auth record). Best-effort record from event data alone.
            // NOTE: top-level clientId ("cust-1001") is assumed to be the customer id here —
            // confirm this mapping with the ingress-service contract.
            fields.putIfAbsent(CUSTOMER_ID, event.getClientId());
            fields.putIfAbsent(CREATED_AT, Instant.now().toString());
            log.warn("No pre-existing session for sid={}, created connectivity-only record", sid);
        }

        redis.opsForHash().putAll(key, fields);
        // Safety-net TTL: re-applied on every CONNECTED/KEEPALIVE so a lost
        // DISCONNECTED event can't leave a client reporting online forever.
        // This was previously commented out, silently defeating the TTL
        // documented in the README/SessionProperties javadoc.
        redis.expire(key, sessionProperties.getSessionIdleTtl());

        log.info("Session connectivity updated: sid={} status={} node={}", sid, status, event.getNodeId());
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private SessionInfo requireSession(String sessionId, String key) {
        SessionInfo session = getSession(sessionId);
        if (session == null) {
            throw new SessionNotFoundException(key + " does not exist!");
        }
        return session;
    }

    private SessionInfo toSessionInfo(Map<Object, Object> map) {
        String reason = (String) map.get(REASON);
        String lastEventTs = (String) map.get(LAST_EVENT_TIMESTAMP);
        String protocol = (String) map.get(PROTOCOL);

        return SessionInfo.builder()
                .sessionId((String) map.get(SESSION_ID))
                .customerId((String) map.get(CUSTOMER_ID))
                .deviceId((String) map.get(DEVICE_ID))
                .clientId((String) map.get(CLIENT_ID))
                .username((String) map.get(USERNAME))
                .ipAddress((String) map.get(IP_ADDRESS))
                .node((String) map.get(NODE))
                .protocol(protocol != null ? Integer.valueOf(protocol) : null)
                .refreshTokenId((String) map.get(REFRESH_TOKEN_ID))
                .createdAt(parseInstant((String) map.get(CREATED_AT)))
                .expireAt(parseInstant((String) map.get(EXPIRE_AT)))
                .lastRefreshAt(parseInstant((String) map.get(LAST_REFRESH_AT)))
                .lastEventTimestamp(lastEventTs != null ? Long.valueOf(lastEventTs) : null)
                .status(SessionStatus.valueOf((String) map.get(STATUS)))
                .reason(reason != null ? SessionReason.valueOf(reason) : SessionReason.NONE)
                .build();
    }

    private Instant parseInstant(String value) {
        return value != null ? Instant.parse(value) : null;
    }

    private String buildSessionKey(String sessionId) {
        return SESSION + sessionId;
    }

    private String buildDeviceSessionKey(String customerId, String deviceId) {
        return DEVICE_SESSION + customerId + ":" + deviceId;
    }

    private String buildClientSessionKey(String clientId) {
        return CLIENT_SESSION + clientId;
    }
}
