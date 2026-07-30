package com.isc.security.service;

import com.isc.security.model.JwtClaims;

public interface JwtValidatorService {

    JwtClaims validate(String jwt);

    JwtClaims validate(String jwt, boolean verifyExpiration);

    boolean isValid(String jwt);
}