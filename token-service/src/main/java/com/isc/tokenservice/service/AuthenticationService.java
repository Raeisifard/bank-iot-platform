package com.isc.tokenservice.service;

import com.isc.common.enums.JwtTokenType;
import com.isc.tokenservice.config.JwtProperties;
import com.isc.tokenservice.dto.*;
import com.isc.tokenservice.identity.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.isc.tokenservice.util.HashUtils.sha256;

/**
 * token-service has no notion of "session" beyond the sessionId string it
 * mints into the JWT (sid claim) and uses as the refresh-token's index key.
 * It never creates, reads, or revokes a session record — session existence
 * and lifecycle (ONLINE/OFFLINE, connectivity, node) belong entirely to
 * session-manager-service, which materializes the Redis record from EMQX
 * CLIENT_CONNECTED/DISCONNECTED events. See SessionServiceImpl in that module.
 *
 * Refresh-token validity (ACTIVE/REVOKED/expiry) is self-contained in
 * RefreshTokenService and no longer cross-checked against a session record —
 * access tokens were already validated statelessly (signature + exp only,
 * see JwtValidatorServiceImpl), so this doesn't weaken that path; it only
 * removes a redundant session lookup from /refresh.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RefreshTokenService refreshTokenService;
    private final JwtGeneratorService jwtGeneratorService;
    private final JwtProperties jwtProperties;

    public AuthTokens login(LoginRequest req) throws Exception {

        // Bare identifier only — no session record is created here.
        // The client's own EMQX connection (using this sid inside its JWT)
        // is what causes session-manager-service to create the actual
        // session row.
        String sessionId = "SID-" + UUID.randomUUID();

        String refreshToken =
                refreshTokenService.create(
                        req.getUserId(),
                        req.getDeviceId(),
                        req.getClientId(),
                        sessionId
                );

        AuthTokens tokens =
                jwtGeneratorService.issue(
                        req.getUserId(),
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

        // No session revocation call — session-manager-service tracks
        // connectivity independently; it will see the client disconnect
        // from EMQX on its own.
    }

    public void handleTokenReuse(String sessionId) {

        refreshTokenService.revokeBySession(sessionId);
    }

    public AuthTokens refresh(RefreshTokenRequest req) throws Exception {

        String refreshTokenHash = sha256(req.getRefreshToken());

        RefreshTokenInfo refreshTokenInfo = refreshTokenService.validate(refreshTokenHash);

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
