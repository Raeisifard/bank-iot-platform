package com.isc.token.keymanagement;

import java.time.Instant;

public record JwtKeyVersion(String keyId, long version, JwtKeyStatus status, Instant createdAt, Instant activatedAt,
                            Instant retiredAt) {
}
