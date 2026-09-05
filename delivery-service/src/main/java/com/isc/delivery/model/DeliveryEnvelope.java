package com.isc.delivery.model;

import java.time.Instant;

public record DeliveryEnvelope(
        String messageId,
        String clientId,
        String payload,
        int deliveryAttempt,
        Instant createdAt) {
}
