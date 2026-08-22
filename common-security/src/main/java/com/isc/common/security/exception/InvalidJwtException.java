package com.isc.common.security.exception;

public class InvalidJwtException extends JwtValidationException {
    public InvalidJwtException(String m) {
        super(m);
    }

    public InvalidJwtException(String m, Throwable t) {
        super(m, t);
    }
}
