package com.isc.common.security.authentication;

import com.isc.common.security.jwt.JwtValidatorService;
import com.isc.common.security.model.SecurityPrincipal;

public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtValidatorService validator;

    public AuthenticationServiceImpl(JwtValidatorService validator) {
        this.validator = validator;
    }

    @Override
    public SecurityPrincipal authenticate(String bearerToken) {
        return validator.validateAndExtractPrincipal(bearerToken);
    }
}
