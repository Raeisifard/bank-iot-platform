package com.isc.contract.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor
@Schema(
        name = "BaseKafkaEvent",
        description = "Base envelope for all Kafka domain events."
)
public abstract class BaseKafkaEvent {

    @Schema(
            description = "Globally unique event identifier.",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String eventId;

    @Schema(
            description = "Event type.",
            implementation = EventType.class,
            example = "CLIENT_CONNECTED"
    )
    private EventType eventType;

    @Schema(
            description = "Event schema version.",
            example = "1"
    )
    private Short version;

    @Schema(
            description = "UTC timestamp when the event was created.",
            example = "2026-06-10T12:34:56Z"
    )
    private Instant eventTime;

    @Schema(
            description = "Source service that produced the event.",
            example = "session-service"
    )
    private String source;

    @Schema(
            description = "Distributed tracing identifier.",
            example = "8f4c7d1a5e8c4e6b9c1d2a3f4b5c6d7e"
    )
    private String traceId;

    @Schema(
            description = "Business correlation identifier used across services.",
            example = "CORR-20260610-10001"
    )
    private String correlationId;

    @Schema(
            description = "Tenant identifier.",
            example = "tenant-001"
    )
    private String tenantId;

    @Schema(
            description = "Actor that initiated the operation (android, ios, pwa, web, etc.).",
            example = "android"
    )
    private String actor;

    @Schema(
            description = "Channel name that this event received from (emqx, api, console, etc.).",
            example = "emqx"
    )
    private String channel;

    @Schema(
            description = "Node, pod, container or server identifier that produced the event.",
            example = "session-service-pod-01"
    )
    private String nodeId;

    @Schema(
            description = "Deployment environment.",
            allowableValues = {
                    "dev",
                    "test",
                    "stage",
                    "prod"
            },
            example = "prod"
    )
    private String environment;

    @Schema(
            description = "Business aggregate category associated with the event.",
            implementation = AggregateType.class,
            example = "SESSION"
    )
    private AggregateType aggregateType;

    @Schema(
            description = "Business aggregate identifier.",
            example = "SESSION-123456"
    )
    private String aggregateId;

    public static String newEventId() {
        return UUID.randomUUID().toString();
    }

    public static Instant now() {
        return Instant.now();
    }
}