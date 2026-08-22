package com.isc.token.exception;

public class KeyRotationException extends TokenServiceException {
    public KeyRotationException(String m, Throwable t) {
        super(m, t);
    }
}
