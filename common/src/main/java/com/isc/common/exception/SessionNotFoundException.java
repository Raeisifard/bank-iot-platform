package com.isc.common.exception;

public class SessionNotFoundException
        extends RuntimeException {

    public SessionNotFoundException(String s) {

        super(s);
    }

    public SessionNotFoundException() {

        super("Session notfound");
    }
}
