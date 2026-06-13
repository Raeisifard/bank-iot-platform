package com.isc.tokenservice.identity;

import com.isc.tokenservice.dto.SessionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final StringRedisTemplate redis;

    @Value("${bank.security.session.ttl-seconds:3600}")
    private long sessionTtlSeconds;

    /**
     * Create authenticated session
     */
    public SessionInfo createSession(
            String customerId,
            String deviceId,
            String clientId,
            String jwtId
    ) {

        String sessionId =
                UUID.randomUUID().toString();

        String key =
                buildSessionKey(sessionId);

        SessionInfo session = SessionInfo.builder()
                .sessionId(sessionId)
                .customerId(customerId)
                .deviceId(deviceId)
                .clientId(clientId)
                .jwtId(jwtId)
                .createdAt(Instant.now())
                .status("ACTIVE")
                .build();

        redis.opsForHash().put(key, "sessionId", session.getSessionId());
        redis.opsForHash().put(key, "customerId", session.getCustomerId());
        redis.opsForHash().put(key, "deviceId", session.getDeviceId());
        redis.opsForHash().put(key, "clientId", session.getClientId());
        redis.opsForHash().put(key, "jwtId", session.getJwtId());
        redis.opsForHash().put(key, "createdAt", session.getCreatedAt().toString());
        redis.opsForHash().put(key, "status", session.getStatus());

        redis.expire(
                key,
                Duration.ofSeconds(sessionTtlSeconds)
        );

        /*
         * mapping:
         * customer + device -> session
         */
        redis.opsForValue().set(
                buildDeviceSessionKey(customerId, deviceId),
                sessionId,
                Duration.ofSeconds(sessionTtlSeconds)
        );

        /*
         * mapping:
         * mqtt clientId -> session
         */
        redis.opsForValue().set(
                buildClientSessionKey(clientId),
                sessionId,
                Duration.ofSeconds(sessionTtlSeconds)
        );

        return session;
    }

    /**
     * Validate session existence and status
     */
    public boolean isValid(String sessionId) {

        String key =
                buildSessionKey(sessionId);

        Boolean exists =
                redis.hasKey(key);

        if (!exists) {
            return false;
        }

        Object status =
                redis.opsForHash().get(key, "status");

        return "ACTIVE".equals(status);
    }

    /**
     * Revoke session
     */
    public void revokeSession(String sessionId) {

        String key =
                buildSessionKey(sessionId);

        redis.opsForHash().put(
                key,
                "status",
                "REVOKED"
        );

        redis.expire(
                key,
                Duration.ofMinutes(5)
        );
    }

    /**
     * Remove session completely
     */
    public void deleteSession(String sessionId) {

        redis.delete(
                buildSessionKey(sessionId)
        );
    }

    /**
     * Extend TTL on activity
     */
    public void refreshSession(String sessionId) {

        String key =
                buildSessionKey(sessionId);

        Boolean exists =
                redis.hasKey(key);

        if (exists) {

            redis.expire(
                    key,
                    Duration.ofSeconds(sessionTtlSeconds)
            );
        }
    }

    /**
     * Get active session of a device
     */
    public String getDeviceSession(
            String customerId,
            String deviceId
    ) {

        return redis.opsForValue().get(
                buildDeviceSessionKey(
                        customerId,
                        deviceId
                )
        );
    }

    /**
     * Store heartbeat timestamp
     */
    public void heartbeat(String sessionId) {

        redis.opsForHash().put(
                buildSessionKey(sessionId),
                "lastSeen",
                Instant.now().toString()
        );
    }

    /**
     * Check concurrent login
     */
    public boolean hasActiveDeviceSession(
            String customerId,
            String deviceId
    ) {

        String sessionId =
                getDeviceSession(customerId, deviceId);

        if (sessionId == null) {
            return false;
        }

        return isValid(sessionId);
    }

    private String buildSessionKey(String sessionId) {
        return "session:" + sessionId;
    }

    private String buildDeviceSessionKey(
            String customerId,
            String deviceId
    ) {
        return "device-session:"
                + customerId
                + ":"
                + deviceId;
    }
    private String buildClientSessionKey(String clientId) {
        return "client-session:" + clientId;
    }
}
