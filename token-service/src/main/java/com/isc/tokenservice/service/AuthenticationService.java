package com.isc.tokenservice.service;

import com.isc.common.dto.SessionInfo;
import com.isc.common.enums.JwtTokenType;
import com.isc.common.enums.SessionStatus;
import com.isc.tokenservice.config.JwtProperties;
import com.isc.tokenservice.dto.*;
import com.isc.common.enums.SessionReason;
import com.isc.tokenservice.exception.InvalidRefreshTokenException;
import com.isc.tokenservice.identity.RefreshTokenService;
import com.isc.tokenservice.identity.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.isc.tokenservice.util.HashUtils.sha256;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final SessionService sessionService;
    private final RefreshTokenService refreshTokenService;
    private final JwtGeneratorService jwtGeneratorService;
    private final JwtProperties jwtProperties;

    public AuthTokens login(LoginRequest req) throws Exception {

        String sessionId = "SID-" + UUID.randomUUID();

        Instant now = Instant.now();

        SessionInfo session = SessionInfo.builder()
                .sessionId(sessionId)
                .customerId(req.getCustomerId())
                .deviceId(req.getDeviceId())
                .clientId(req.getClientId())
                .createdAt(now)
                .expireAt(now.plus(jwtProperties.getSessionAbsoluteTtl()))
                .lastRefreshAt(now)
                .status(SessionStatus.ONLINE)
                .reason(SessionReason.NONE)
                .build();

        sessionService.create(session);

        String refreshToken =
                refreshTokenService.create(
                        req.getCustomerId(),
                        req.getDeviceId(),
                        req.getClientId(),
                        sessionId
                );

        AuthTokens tokens =
                jwtGeneratorService.issue(
                        req.getCustomerId(),
                        req.getDeviceId(),
                        req.getClientId(),
                        sessionId,
                        JwtTokenType.ACCESS_TOKEN
                );

        tokens.setRefreshToken(refreshToken);
        tokens.setRefreshTokenExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenTtl()));
        tokens.setTokenType("Bearer");

        return tokens;
    }

    public void logout(String sessionId) {

        refreshTokenService.revokeBySession(sessionId);

        sessionService.revokeSession(
                sessionId,
                SessionReason.USER_LOGOUT
        );
    }

    public void handleTokenReuse(String sessionId) {

        refreshTokenService.revokeBySession(sessionId);

        sessionService.revokeSession(
                sessionId,
                SessionReason.REFRESH_REUSE
        );
    }

    public AuthTokens refresh(RefreshTokenRequest req) throws Exception {

        String refreshTokenHash = sha256(req.getRefreshToken());

        RefreshTokenInfo refreshTokenInfo = refreshTokenService.validate(refreshTokenHash);

        if (!sessionService.isValid(refreshTokenInfo.getSessionId())) {
            throw new InvalidRefreshTokenException(
                    "Session is not valid");
        }

        RefreshRotationResult result = refreshTokenService.rotate(refreshTokenInfo);

        AuthTokens tokens = jwtGeneratorService.issue(
                result.getCustomerId(),
                result.getDeviceId(),
                result.getClientId(),
                result.getSessionId(),
                JwtTokenType.AUTHENTICATION_TOKEN
        );

        tokens.setRefreshToken(
                result.getRefreshToken()
        );

        return tokens;
    }

}