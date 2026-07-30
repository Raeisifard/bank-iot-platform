package com.isc.security.exception;

public class InvalidTokenException
        extends JwtValidationException {

    public InvalidTokenException(Exception e) {
        super("Invalid token", e);
    }
    public InvalidTokenException(String msg) {
        super(msg);
    }
    public InvalidTokenException() {
        super("Invalid token");
    }
}
