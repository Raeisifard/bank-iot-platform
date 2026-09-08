package com.isc.clientidentity.model;

import java.time.Instant;

public record ClientChallenge(String id, String cid, String nonce, Instant expiresAt) {
}