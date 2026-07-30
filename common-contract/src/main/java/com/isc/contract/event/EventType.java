package com.isc.contract.event;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported event types")
public enum EventType {

    /**
     * Lifecycle event types published by the Kafka Ingress Service, sourced from
     * EMQX client connect/disconnect webhooks (and optionally keepalive pings).
     */
    CLIENT_CONNECTED,
    CLIENT_DISCONNECTED,
    CLIENT_KEEPALIVE,

    SESSION_CREATED,
    SESSION_EXPIRED,
    SESSION_TERMINATED,

    JWT_ISSUED,
    JWT_REFRESHED,
    JWT_REVOKED,

    DEVICE_REGISTERED,
    DEVICE_REMOVED
}
