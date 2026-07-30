package com.isc.sessionmanager.service;

import com.isc.contract.event.EventType;
import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.sessionmanager.model.ClientSession;
import com.isc.sessionmanager.repository.SessionRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final SessionRedisRepository sessionRepository;

    @Override
    public void handleConnectionEvent(ClientConnectedEvent event) {
        Optional<ClientSession> existing = sessionRepository.find(event.());

        if (isStale(event, existing)) {
            log.info("Discarding stale/out-of-order event: phoneNumber={} eventType={} eventTs={} " +
                            "currentSessionTs={} (an event carrying an older timestamp than the " +
                            "currently stored session was received)",
                    event.phoneNumber(), event.eventType(), event.timestamp(),
                    existing.map(ClientSession::lastEventTimestamp).orElse(null));
            return;
        }

        switch (event.getEventType()) {
            case EventType.CLIENT_CONNECTED -> handleConnected(event);
            case EventType.CLIENT_DISCONNECTED -> handleDisconnected(event);
            case EventType.CLIENT_KEEPALIVE -> handleKeepalive(event, existing);
        }
    }

    /**
     * Guards against applying an event that is older than what's already reflected
     * in Redis — protects against Kafka redelivery / out-of-order consumer-group
     * rebalances producing a "time travel" write.
     */
    private boolean isStale(ClientConnectedEvent event, Optional<ClientSession> existing) {
        return existing
                .map(session -> event.timestamp() < session.lastEventTimestamp())
                .orElse(false);
    }

    private void handleConnected(ClientConnectedEvent event) {
        ClientSession session = ClientSession.connected(event, Instant.now().toEpochMilli());
        sessionRepository.save(event.phoneNumber(), session);
        log.info("Session CREATED/UPDATED (online): phoneNumber={} clientId={} node={}",
                event.phoneNumber(), event.clientId(), event.node());
    }

    private void handleDisconnected(ClientConnectedEvent event) {
        // Retention policy choice: keep an OFFLINE record briefly (useful for "last seen"
        // queries) rather than deleting immediately. Redis TTL will reap it eventually.
        // Swap to sessionRepository.delete(...) if downstream consumers only ever need
        // "is currently online" semantics.
        ClientSession session = ClientSession.disconnected(event, Instant.now().toEpochMilli());
        sessionRepository.save(event.phoneNumber(), session);
        log.info("Session UPDATED (offline): phoneNumber={} clientId={} node={}",
                event.phoneNumber(), event.clientId(), event.node());
    }

    private void handleKeepalive(ClientConnectedEvent event, Optional<ClientSession> existing) {
        // Refresh TTL / lastEventTimestamp without changing status, and self-heal if
        // a CONNECTED event was somehow missed but keepalives are still arriving.
        ClientSession session = ClientSession.connected(event, Instant.now().toEpochMilli());
        sessionRepository.save(event.phoneNumber(), session);
        log.debug("Session refreshed via KEEPALIVE: phoneNumber={} clientId={}",
                event.phoneNumber(), event.clientId());
    }
}
