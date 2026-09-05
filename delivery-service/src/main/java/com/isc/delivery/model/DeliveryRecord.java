package com.isc.delivery.model;

import java.time.Instant;

public record DeliveryRecord(
        String messageId,
        String clientId,
        String payload,
        int attempt,
        DeliveryState state,
        Instant createdAt,
        Instant updatedAt,
        String failureReason) {
}
