package com.isc.token.service;

import com.isc.token.config.TokenServiceProperties;
import com.isc.token.dto.TokenRequest;
import com.isc.token.dto.TokenResponse;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {
    private final JwtGeneratorService generator;
    private final TokenServiceProperties properties;

    public TokenServiceImpl(JwtGeneratorService g, TokenServiceProperties p) {
        generator = g;
        properties = p;
    }

    public TokenResponse issue(TokenRequest r) {
        return new TokenResponse(generator.generateAccessToken(r), "Bearer", properties.getAccessTokenLifetime().toSeconds(), generator.generateRefreshToken(r));
    }
}
