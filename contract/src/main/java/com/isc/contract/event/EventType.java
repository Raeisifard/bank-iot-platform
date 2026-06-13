package com.isc.contract.event;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported event types")
public enum EventType {

    CLIENT_CONNECTED,
    CLIENT_DISCONNECTED,

    SESSION_CREATED,
    SESSION_EXPIRED,
    SESSION_TERMINATED,

    JWT_ISSUED,
    JWT_REFRESHED,
    JWT_REVOKED,

    DEVICE_REGISTERED,
    DEVICE_REMOVED
}
