package com.isc.tokenservice.identity;

import com.isc.common.enums.SessionStatus;
import com.isc.tokenservice.config.JwtProperties;
import com.isc.common.dto.SessionInfo;
import com.isc.common.enums.SessionReason;
import com.isc.common.exception.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.isc.common.constants.RedisKeys.*;
import static com.isc.common.constants.ClientSessionFieldsName.*;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final StringRedisTemplate redis;
    private final JwtProperties jwtProperties;

    /**
     * Create authenticated session
     */
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

        if (session.getStatus() == null) {
            values.put(STATUS, SessionStatus.ONLINE.name());
        } else {
            values.put(STATUS, session.getStatus().name());
        }
        if (session.getReason() != null) {
            values.put(REASON, session.getReason().name());
        }

        redis.opsForHash().putAll(key, values);

        Duration ttl = jwtProperties.getSessionIdleTtl().plus(jwtProperties.getSessionAuditTtl());

        redis.expire(key, ttl);

        redis.opsForValue().set(
                buildDeviceSessionKey(session.getCustomerId(), session.getDeviceId()),
                session.getSessionId(),
                ttl
        );

        redis.opsForValue().set(
                buildClientSessionKey(session.getClientId()),
                session.getSessionId(),
                ttl
        );

        return session.getSessionId();
    }

    /**
     * Retrieve session
     */
    public SessionInfo getSession(String sessionId) {

        String key = buildSessionKey(sessionId);
        Map<Object, Object> map = redis.opsForHash().entries(key);
        if (map.isEmpty()) {
            return null;
        }
        String reason = (String) map.get(REASON);
        return SessionInfo.builder()
                .sessionId((String) map.get(SESSION_ID))
                .customerId((String) map.get(CUSTOMER_ID))
                .deviceId((String) map.get(DEVICE_ID))
                .clientId((String) map.get(CLIENT_ID))
                .refreshTokenId((String) map.get(REFRESH_TOKEN_ID))
                .createdAt(Instant.parse((String) map.get(CREATED_AT)))
                .expireAt(Instant.parse((String) map.get(EXPIRE_AT)))
                .lastRefreshAt(Instant.parse((String) map.get(LAST_REFRESH_AT)))
                .status(SessionStatus.valueOf((String) map.get(STATUS)))
                .reason(reason != null ? SessionReason.valueOf(reason) : SessionReason.NONE)
                .build();
    }

    /**
     * Validate session existence and status
     */
    public boolean isValid(String sessionId) {
        SessionInfo session = getSession(sessionId);
        if (session == null) {
            return false;
        }
        if (session.getStatus() == null || session.getStatus() != SessionStatus.ONLINE) {
            return false;
        }
        if (session.getExpireAt().isBefore(Instant.now())) {
            return false;
        }
        if(session.getLastRefreshAt().plus(jwtProperties.getSessionIdleTtl()).isBefore(Instant.now())) {
            return false;
        }
        return !session.getLastRefreshAt().plus(jwtProperties.getSessionIdleTtl()).isBefore(Instant.now());
    }

    /**
     * Revoke session
     */
    public void revokeSession(String sessionId, SessionReason reason) {

        String key = buildSessionKey(sessionId);
        SessionInfo session = getSession(sessionId);
        if (session == null) {
            throw new SessionNotFoundException(key + " does not exist!");
        }
        if (session.getStatus() == SessionStatus.REVOKED) {
            return;
        }
        redis.opsForHash().put(key, STATUS, SessionStatus.REVOKED.name());
        redis.opsForHash().put(key, REASON, reason != null ? reason.name() : SessionReason.NONE.name());
        redis.delete(buildDeviceSessionKey(session.getCustomerId(), session.getDeviceId()));
        redis.delete(buildClientSessionKey(session.getClientId()));
    }

    /**
     * Remove session completely
     */
    public void deleteSession(String sessionId) {
        String key = buildSessionKey(sessionId);
        SessionInfo session = getSession(sessionId);
        if (session == null) {
            throw new SessionNotFoundException(key + " does not exist!");
        }
        redis.delete(key);
        redis.delete(buildDeviceSessionKey(session.getCustomerId(), session.getDeviceId()));
        redis.delete(buildClientSessionKey(session.getClientId()));
    }

    /**
     * Extend TTL on activity
     */
    public void refreshSession(String sessionId) {

        String key = buildSessionKey(sessionId);
        SessionInfo session = getSession(sessionId);
        if (session == null) {
            throw new SessionNotFoundException(key + " do not exist!");
        }
        Duration ttl = jwtProperties.getSessionIdleTtl();
        //redis.expire(key, ttl);
        //redis.expire(buildDeviceSessionKey(session.getCustomerId(), session.getDeviceId()), ttl);
        //redis.expire(buildClientSessionKey(session.getClientId()), ttl);
        redis.opsForHash().put(key, LAST_REFRESH_AT, Instant.now().toString());
    }

    /**
     * Update last activity time of session.
     * Does NOT extend session expiration.
     */
    public void touchSession(String sessionId) {

        String key = buildSessionKey(sessionId);

        if (!redis.hasKey(key)) {
            throw new SessionNotFoundException(
                    key + " does not exist!"
            );
        }

        redis.opsForHash().put(
                key,
                LAST_REFRESH_AT,
                Instant.now().toString()
        );
    }

    /**
     * Get active session of a device
     */
    public String getDeviceSession(String customerId, String deviceId) {
        return redis.opsForValue().get(buildDeviceSessionKey(customerId, deviceId));
    }

    /**
     * Check concurrent login
     */
    public boolean hasActiveDeviceSession(String customerId, String deviceId) {
        String sessionId = getDeviceSession(customerId, deviceId);
        if (sessionId == null) {
            return false;
        }
        return isValid(sessionId);
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

    public String getClientSession(String clientId) {
        return redis.opsForValue().get(buildClientSessionKey(clientId));
    }
}
