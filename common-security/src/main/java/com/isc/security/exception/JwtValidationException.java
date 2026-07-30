package com.isc.security.exception;

public class JwtValidationException
        extends RuntimeException {

    public JwtValidationException(String message, Exception e) {
        super(message, e);
    }

    public JwtValidationException(String message) {
        super(message);
    }

    public JwtValidationException() {
        super();
    }
}
