package com.isc.tokenservice.exception;

public class InactiveSessionException
        extends RuntimeException {

    public InactiveSessionException() {

        super("Session inactive");
    }
}