package com.isc.common.security.exception;

public class SecurityConfigurationException extends SecurityException {
    public SecurityConfigurationException(String m) {
        super(m);
    }

    public SecurityConfigurationException(String m, Throwable t) {
        super(m, t);
    }
}
