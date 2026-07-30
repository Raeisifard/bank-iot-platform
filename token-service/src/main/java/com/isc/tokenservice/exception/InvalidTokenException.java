package com.isc.tokenservice.exception;

public class InvalidTokenException
        extends RuntimeException {

    public InvalidTokenException(String msg) {

        super(msg);
    }

    public InvalidTokenException() {

        super("Invalid token");
    }
}