package com.isc.sessionmanager.service;

import com.isc.contract.event.EventType;
import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.sessionmanager.model.ClientSession;
import com.isc.sessionmanager.repository.SessionRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SessionServiceImplTest {

    private SessionRedisRepository repository;
    private SessionServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(SessionRedisRepository.class);
        service = new SessionServiceImpl(repository);
    }

    @Test
    void connectedEvent_createsOnlineSession() {
        ClientConnectedEvent event = new ClientConnectedEvent(
                "client-1", "+989120000000", EventType.CLIENT_CONNECTED, "emqx@node1", "mqtt", 1000L);
        when(repository.find("+989120000000")).thenReturn(Optional.empty());

        service.handleConnectionEvent(event);

        ArgumentCaptor<ClientSession> captor = ArgumentCaptor.forClass(ClientSession.class);
        verify(repository).save(eq("+989120000000"), captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(ClientSession.SessionStatus.ONLINE);
        assertThat(captor.getValue().clientId()).isEqualTo("client-1");
    }

    @Test
    void disconnectedEvent_marksSessionOffline() {
        ClientConnectedEvent event = new ClientConnectedEvent(
                "client-1", "+989120000000", EventType.CLIENT_DISCONNECTED, "emqx@node1", "mqtt", 2000L);
        when(repository.find("+989120000000")).thenReturn(Optional.empty());

        service.handleConnectionEvent(event);

        ArgumentCaptor<ClientSession> captor = ArgumentCaptor.forClass(ClientSession.class);
        verify(repository).save(eq("+989120000000"), captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(ClientSession.SessionStatus.OFFLINE);
    }

    @Test
    void staleEvent_isDiscardedAndNotSaved() {
        ClientSession existingNewer = new ClientSession(
                "client-1", "+989120000000", "emqx@node1", "mqtt",
                ClientSession.SessionStatus.ONLINE, 5000L, 5000L);
        when(repository.find("+989120000000")).thenReturn(Optional.of(existingNewer));

        // This DISCONNECTED event has an older timestamp than what's already stored,
        // simulating out-of-order Kafka delivery.
        ClientConnectedEvent staleEvent = new ClientConnectedEvent(
                "client-1", "+989120000000", EventType.CLIENT_DISCONNECTED, "emqx@node1", "mqtt", 1000L);

        service.handleConnectionEvent(staleEvent);

        verify(repository, never()).save(anyString(), any());
    }

    @Test
    void keepaliveEvent_refreshesSessionWithoutChangingSemantics() {
        ClientConnectedEvent event = new ClientConnectedEvent(
                "client-1", "+989120000000", EventType.CLIENT_KEEPALIVE, "emqx@node1", "mqtt", 3000L);
        when(repository.find("+989120000000")).thenReturn(Optional.empty());

        service.handleConnectionEvent(event);

        ArgumentCaptor<ClientSession> captor = ArgumentCaptor.forClass(ClientSession.class);
        verify(repository).save(eq("+989120000000"), captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(ClientSession.SessionStatus.ONLINE);
    }
}
