package com.isc.contract.event.session;

import com.isc.contract.event.AggregateType;
import com.isc.contract.event.BaseKafkaEvent;
import com.isc.contract.event.EventType;

public final class SessionEvents {

    private SessionEvents() {
    }

    public static ClientConnectedEvent.ClientConnectedEventBuilder<?, ?>
    connectedEventBuilder(
            String clientId) {

        return ClientConnectedEvent.builder()
                .eventId(BaseKafkaEvent.newEventId())
                .eventType(EventType.CLIENT_CONNECTED)
                .version((short)1)
                .eventTime(BaseKafkaEvent.now())
                .aggregateType(AggregateType.CUSTOMER)
                .aggregateId(clientId);
    }
}