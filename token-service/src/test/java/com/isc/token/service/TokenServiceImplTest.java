package com.isc.token.service;

import com.isc.token.config.TokenServiceProperties;
import com.isc.token.dto.TokenRequest;
import com.isc.token.dto.TokenResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenServiceImplTest {

    @Test
    void shouldIssueAccessAndRefreshTokens() {

        TokenServiceProperties properties =
                new TokenServiceProperties();

        JwtGeneratorService generator =
                mock(JwtGeneratorService.class);

        when(generator.generateAccessToken(
                org.mockito.ArgumentMatchers.any()))
                .thenReturn("access.jwt");

        when(generator.generateRefreshToken(
                org.mockito.ArgumentMatchers.any()))
                .thenReturn("refresh.jwt");

        TokenServiceImpl service =
                new TokenServiceImpl(generator, properties);

        TokenRequest request = new TokenRequest();
        request.setClientId("client");

        TokenResponse response =
                service.issue(request);

        assertEquals("access.jwt", response.accessToken());
        assertEquals("refresh.jwt", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(
                properties.getAccessTokenLifetime().toSeconds(),
                response.expiresIn());
    }
}
