package com.isc.common.security.exception;

public class MissingJwtException extends JwtValidationException {
    public MissingJwtException(String m) {
        super(m);
    }

    public MissingJwtException(String m, Throwable t) {
        super(m, t);
    }
}
