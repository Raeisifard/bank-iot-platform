package com.isc.common.exception;

public class ResourceNotFoundException
        extends BusinessException {

    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message);
    }

}
