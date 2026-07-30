package com.isc.tokenservice.exception;

public class RevokedTokenException
        extends RuntimeException {

    public RevokedTokenException() {

        super("Token revoked");
    }
}