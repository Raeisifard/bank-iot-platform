package com.isc.common.security.jwt;

import com.isc.common.security.model.JwtValidationResult;
import com.isc.common.security.model.SecurityPrincipal;

public interface JwtValidatorService {
    JwtValidationResult validate(String token);

    SecurityPrincipal validateAndExtractPrincipal(String token);

    boolean isValid(String token);
}
