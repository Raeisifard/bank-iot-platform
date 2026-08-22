package com.isc.common.security.exception;

public class JwtValidationException extends SecurityException {
    public JwtValidationException(String message) {
        super(message);
    }

    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
