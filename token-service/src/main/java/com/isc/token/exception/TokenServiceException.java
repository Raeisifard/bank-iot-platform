package com.isc.token.exception;

public class TokenServiceException extends RuntimeException {
    public TokenServiceException(String m) {
        super(m);
    }

    public TokenServiceException(String m, Throwable t) {
        super(m, t);
    }
}
