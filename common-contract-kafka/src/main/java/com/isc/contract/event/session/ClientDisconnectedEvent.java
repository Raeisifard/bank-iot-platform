package com.isc.contract.event.session;

import com.isc.common.dto.ClientAttributes;
import com.isc.contract.event.BaseKafkaEvent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Client connection lifecycle event published by the ingress service
 * (topic: client-connection-events). Covers CONNECTED / DISCONNECTED /
 * KEEPALIVE (event type comes from BaseKafkaEvent.eventType).
 *
 * Session identity comes from {@code jwt.sid}, not from clientId/username —
 * clientId here is the MQTT client id, not a stable session key.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDisconnectedEvent extends BaseKafkaEvent {

    private String clientId;     // MQTT client id (e.g. "cust-1001")
    private String username;
    private String ipAddress;
    private Integer protocol;    // MQTT protocol level (5 = MQTT5)
    private Long connectedAt;      // event-producer epoch millis, used for stale/out-of-order guard
    private Long disconnectedAt;      // event-producer epoch millis, used for stale/out-of-order guard

    @NotNull
    private ClientAttributes jwt;         // jwt.sid is mandatory: it's the session lookup key
}
