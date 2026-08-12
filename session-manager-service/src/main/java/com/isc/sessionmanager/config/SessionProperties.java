package com.isc.sessionmanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Binds app.session.* from application.yml. Lives in session-manager since
 * this module currently owns all session read/write logic; token-service
 * will get its own config (or reuse this one via grpc) once the shared
 * session API is exposed.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.session")
public class SessionProperties {

    /** app.session.key-prefix */
    private String keyPrefix = "session:";

    /**
     * app.session.phone-index-prefix â€” legacy phone-based index.
     * No longer written (sessions are keyed by jwt.sid now); kept only so
     * old records can still be read/migrated if needed.
     */
    @Deprecated
    private String phoneIndexPrefix = "session:phone:";

    /**
     * app.session.ttl-seconds â€” safety-net expiry re-applied on every
     * connectivity update (connected/disconnected/keepalive), in case a
     * disconnect event is ever lost. Keep comfortably larger than the
     * EMQX keepalive interval.
     */
    private long ttlSeconds = 180;

    private Redis redis = new Redis();

    @Getter
    @Setter
    public static class Redis {
        /** app.session.redis.session-prefix (currently duplicates key-prefix â€” reconcile the two) */
        private String sessionPrefix = "session:";
        private String clientSessionsPrefix = "client-sessions:";
        private String onlineUsersKey = "online-users";
        /** app.session.redis.default-ttl-hours â€” extra audit retention on top of idle TTL */
        private long defaultTtlHours = 24;
    }

    /** TTL applied on connectivity refresh (connect/disconnect/keepalive). */
    public Duration getSessionIdleTtl() {
        return Duration.ofSeconds(ttlSeconds);
    }

    /** Additional retention window kept for audit purposes after login/creation. */
    public Duration getSessionAuditTtl() {
        return Duration.ofHours(redis.getDefaultTtlHours());
    }
}
