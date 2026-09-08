package com.isc.clientidentity.model;

import java.time.Instant;

public record ClientIdentity(
        String cid,
        String did,
        String publicKey,
        String keyAlgorithm,
        String attestation,
        ClientStatus status,
        Instant registeredAt) {
}