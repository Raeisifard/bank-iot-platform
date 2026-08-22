package com.isc.token.keymanagement;

public record JwtKeyMetadata(String keyName, String keyId, long version, String algorithm, JwtKeyStatus status) {
}
