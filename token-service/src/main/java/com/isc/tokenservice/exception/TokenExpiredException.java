package com.isc.tokenservice.exception;

public class TokenExpiredException
        extends RuntimeException {

    public TokenExpiredException() {

        super("Token expired");
    }
}