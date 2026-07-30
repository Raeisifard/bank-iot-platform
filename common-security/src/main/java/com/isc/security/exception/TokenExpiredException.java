package com.isc.security.exception;

public class TokenExpiredException
        extends JwtValidationException {

    public TokenExpiredException() {
        super("Token expired");
    }
}