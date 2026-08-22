package com.isc.sessionmanager.repository;

import com.isc.common.dto.ClientSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Owns the Redis read/write path for client sessions.
 *
 * Key schema:
 *   session:<phoneNumber>        -> ClientSession (JSON), TTL = app.session.ttl-seconds
 *
 * phoneNumber is used as the canonical key because EMQX/upstream systems resolve
 * client identity from phone number (bank transaction linkage). clientId is kept
 * as an attribute on the session value for reverse lookup/debugging.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SessionRedisRepository {

    private final RedisTemplate<String, ClientSession> redisTemplate;

    @Value("${app.session.key-prefix:session:}")
    private String keyPrefix;

    @Value("${app.session.ttl-seconds:180}")
    private long ttlSeconds;

    private String keyFor(String phoneNumber) {
        return keyPrefix + phoneNumber;
    }

    public Optional<ClientSession> find(String phoneNumber) {
        ClientSession session = redisTemplate.opsForValue().get(keyFor(phoneNumber));
        return Optional.ofNullable(session);
    }

    /**
     * Persists the session and (re)sets the TTL. Used both for CONNECTED (fresh session)
     * and OFFLINE-with-retention writes.
     */
    public void save(String phoneNumber, ClientSession session) {
        redisTemplate.opsForValue().set(keyFor(phoneNumber), session, Duration.ofSeconds(ttlSeconds));
        log.debug("Session saved for phoneNumber={} status={} node={}",
                phoneNumber, session.getStatus(), session.getServerNodeId());
    }

    public void delete(String phoneNumber) {
        Boolean deleted = redisTemplate.delete(keyFor(phoneNumber));
        log.debug("Session delete for phoneNumber={} result={}", phoneNumber, deleted);
    }
}
