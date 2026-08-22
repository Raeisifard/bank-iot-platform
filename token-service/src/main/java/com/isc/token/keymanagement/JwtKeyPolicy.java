package com.isc.token.keymanagement;

import java.time.Duration;

public record JwtKeyPolicy(String keyName, String algorithm, Duration rotationInterval, Duration publicationOverlap,
                           int maxPublishedVersions) {
}
