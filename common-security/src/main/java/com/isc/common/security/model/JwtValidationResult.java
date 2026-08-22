package com.isc.common.security.model;

import java.util.Map;

public record JwtValidationResult(boolean valid, String subject, String issuer, String keyId,
                                  Map<String, Object> claims) {
}
