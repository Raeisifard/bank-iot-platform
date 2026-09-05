package com.isc.sessionmanager.service;

import com.isc.common.dto.ClientAttributes;
import com.isc.common.enums.SessionStatus;
import com.isc.contract.event.EventType;
import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.contract.event.session.ClientDisconnectedEvent;
import com.isc.sessionmanager.config.SessionProperties;
import com.isc.common.redis.RedisOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;

import static com.isc.common.constants.ClientSessionFieldsName.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Covers the idempotency / stale-event / connectivity-transition logic in
 * SessionServiceImpl, as documented in the README. This module previously
 * had no src/test directory at all despite pom.xml and the README both
 * referencing this test.
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private RedisOperations redis;

    private SessionProperties sessionProperties;
    private SessionServiceImpl service;

    private static final String SID = "SID-123";
    private static final String KEY = "session:" + SID;

    @BeforeEach
    void setUp() {
        sessionProperties = new SessionProperties();
        sessionProperties.setTtlSeconds(180);
        service = new SessionServiceImpl(redis, sessionProperties);
    }

    private ClientAttributes jwt() {
        ClientAttributes jwt = new ClientAttributes();
        jwt.setSid(SID);
        jwt.setDid("DEVICE-1");
        jwt.setCid("CUST-1002");
        return jwt;
    }

    private ClientConnectedEvent connectedEvent(long connectedAt) {
        return ClientConnectedEvent.builder()
                .eventType(EventType.CLIENT_CONNECTED)
                .clientId("CUST-1002")
                .username("behnam")
                .ipAddress("192.168.1.1")
                .protocol(5)
                .nodeId("emqx@10.0.1.5")
                .connectedAt(connectedAt)
                .jwt(jwt())
                .build();
    }

    private ClientDisconnectedEvent disconnectedEvent(long disconnectedAt) {
        return ClientDisconnectedEvent.builder()
                .eventType(EventType.CLIENT_DISCONNECTED)
                .clientId("CUST-1002")
                .username("behnam")
                .ipAddress("192.168.1.1")
                .protocol(5)
                .nodeId("emqx@10.0.1.5")
                .disconnectedAt(disconnectedAt)
                .jwt(jwt())
                .build();
    }

    // ---------------------------------------------------------------
    // handleConnectionEvent
    // ---------------------------------------------------------------

    @Test
    void handleConnectionEvent_createsConnectivityOnlyRecord_whenNoExistingSession() {
        when(redis.entries(KEY)).thenReturn(Map.of());

        service.handleConnectionEvent(connectedEvent(1_000L));

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(redis).putAll(eq(KEY), captor.capture());
        Map<String, String> saved = captor.getValue();

        assertThat(saved.get(SESSION_ID)).isEqualTo(SID);
        assertThat(saved.get(STATUS)).isEqualTo(SessionStatus.ONLINE.name());
        assertThat(saved.get(LAST_EVENT_TIMESTAMP)).isEqualTo("1000");
        // TTL must be refreshed on every connectivity update — this was
        // previously commented out in applyConnectivity().
        verify(redis).expire(KEY, Duration.ofSeconds(180));
    }

    @Test
    void handleConnectionEvent_discardsStaleEvent() {
        when(redis.entries(KEY)).thenReturn(Map.of(LAST_EVENT_TIMESTAMP, "5000"));

        service.handleConnectionEvent(connectedEvent(1_000L)); // older than 5000

        verify(redis, never()).putAll(anyString(), anyMap());
        verify(redis, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void handleConnectionEvent_appliesNewerEvent() {
        when(redis.entries(KEY)).thenReturn(Map.of(
            LAST_EVENT_TIMESTAMP, "1000",
            CUSTOMER_ID, "cust-1001"));

        service.handleConnectionEvent(connectedEvent(5_000L));

        verify(redis).putAll(eq(KEY), anyMap());
        verify(redis).expire(KEY, Duration.ofSeconds(180));
    }

    // ---------------------------------------------------------------
    // handleDisconnectionEvent
    // ---------------------------------------------------------------

    @Test
    void handleDisconnectionEvent_ignoredWhenSessionDoesNotExist() {
        when(redis.entries(KEY)).thenReturn(Map.of());

        service.handleDisconnectionEvent(disconnectedEvent(1_000L));

        verify(redis, never()).putAll(anyString(), anyMap());
    }

    @Test
    void handleDisconnectionEvent_discardsStaleEvent() {
        when(redis.entries(KEY)).thenReturn(Map.of(LAST_EVENT_TIMESTAMP, "5000"));

        service.handleDisconnectionEvent(disconnectedEvent(1_000L)); // older than 5000

        verify(redis, never()).putAll(anyString(), anyMap());
    }

    @Test
    void handleDisconnectionEvent_marksSessionOffline() {
        when(redis.entries(KEY)).thenReturn(Map.of(
            LAST_EVENT_TIMESTAMP, "1000",
            STATUS, SessionStatus.ONLINE.name()));

        service.handleDisconnectionEvent(disconnectedEvent(5_000L));

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(redis).putAll(eq(KEY), captor.capture());
        Map<String, String> saved = captor.getValue();

        assertThat(saved.get(STATUS)).isEqualTo(SessionStatus.OFFLINE.name());
        assertThat(saved.get(LAST_EVENT_TIMESTAMP)).isEqualTo("5000");
    }

    // ---------------------------------------------------------------
    // getSession / isValid
    // ---------------------------------------------------------------

    @Test
    void getSession_returnsNull_whenNoRecord() {
        when(redis.entries(KEY)).thenReturn(Map.of());

        assertThat(service.getSession(SID)).isNull();
    }
}
