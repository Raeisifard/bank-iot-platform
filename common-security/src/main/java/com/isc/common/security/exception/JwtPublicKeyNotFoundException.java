package com.isc.common.security.exception;

public class JwtPublicKeyNotFoundException extends SecurityException {
    public JwtPublicKeyNotFoundException(String kid) {
        super("JWT public key not found for kid: " + kid);
    }

    public JwtPublicKeyNotFoundException(String kid, Throwable cause) {
        super("JWT public key not found for kid: " + kid, cause);
    }
}
