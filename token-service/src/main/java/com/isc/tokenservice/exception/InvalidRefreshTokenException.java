package com.isc.tokenservice.exception;

public class InvalidRefreshTokenException
        extends RuntimeException {

    public InvalidRefreshTokenException() {

        super("Session inactive");
    }

    public InvalidRefreshTokenException(String refreshTokenNotFound) {
        super(refreshTokenNotFound);
    }
}