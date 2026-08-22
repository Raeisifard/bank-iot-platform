package com.isc.token.model;

import java.util.Map;

public record TokenClaims(String subject, String clientId, String tokenType, Map<String, Object> additionalClaims) {
}
