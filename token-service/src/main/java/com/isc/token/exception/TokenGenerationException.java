package com.isc.token.exception;

public class TokenGenerationException extends TokenServiceException {
    public TokenGenerationException(String m, Throwable t) {
        super(m, t);
    }
}
