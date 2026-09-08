package com.isc.clientidentity.api;

import java.time.Instant;

public record ChallengeResponse(String challengeId, String cid, String nonce, Instant expiresAt) {
}