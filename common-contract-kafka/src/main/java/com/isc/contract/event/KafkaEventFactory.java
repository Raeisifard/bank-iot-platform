package com.isc.contract.event;

import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.contract.event.session.ClientDisconnectedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class KafkaEventFactory {

    @Value("${spring.application.name}")
    private String source;

    @Value("${app.environment:dev}")
    private String environment;

    @Value("${HOSTNAME:${COMPUTERNAME:unknown}}")
    private String nodeId;

    public ClientConnectedEvent.ClientConnectedEventBuilder<?, ?>
    clientConnected(String sessionId) {

        String eventId = UUID.randomUUID().toString();

        return ClientConnectedEvent.builder()
                .eventId(eventId)
                .eventType(EventType.CLIENT_CONNECTED)
                .version((short) 1)
                .eventTime(Instant.now())

                .source(source)
                .environment(environment)

                // Event Metadata
                .correlationId(eventId)
                .nodeId(nodeId)
                .tenantId("default")

                // Business Metadata
                .aggregateType(AggregateType.SESSION)
                .aggregateId(sessionId);
    }

    public ClientDisconnectedEvent.ClientDisconnectedEventBuilder<?, ?>
    clientDisconnected(String sessionId) {

        String eventId = UUID.randomUUID().toString();

        return ClientDisconnectedEvent.builder()
                .eventId(eventId)
                .eventType(EventType.CLIENT_DISCONNECTED)
                .version((short) 1)
                .eventTime(Instant.now())

                .source(source)
                .environment(environment)

                // Event Metadata
                .correlationId(eventId)
                .nodeId(nodeId)
                .tenantId("default")

                // Business Metadata
                .aggregateType(AggregateType.SESSION)
                .aggregateId(sessionId);
    }

}