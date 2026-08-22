package com.isc.token.service;

import com.isc.token.dto.TokenRequest;

public interface JwtGeneratorService {
    String generateAccessToken(TokenRequest request);

    String generateRefreshToken(TokenRequest request);
}
