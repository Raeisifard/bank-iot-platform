package com.isc.common.response;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message
) {
}
