package com.isc.tokenservice.service;

import com.isc.tokenservice.dto.AuthTokens;
import com.isc.tokenservice.identity.SessionService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtService jwtService;
    private final JwtGeneratorService jwtGeneratorService;
    private final SessionService sessionService;
    private final RefreshRegistryService refreshRegistryService;

    public AuthTokens refresh(String refreshToken)
            throws Exception {

        Claims claims =
                jwtService.parseRefreshToken(refreshToken);

        String sessionId =
                claims.get("sid", String.class);

        String refreshJti =
                claims.getId();

        SessionRegistry session =
                sessionService.load(sessionId);

        if (session == null) {
            throw new RuntimeException("Session not found");
        }

        if (!"ACTIVE".equals(session.getStatus())) {
            throw new RuntimeException("Session revoked");
        }

        RefreshRecord refreshRecord =
                refreshRegistryService.find(refreshJti);

        if (refreshRecord == null) {
            throw new RuntimeException("Refresh not found");
        }

        if (refreshRecord.isUsed()) {

            revokeWholeSession(sessionId);

            throw new RuntimeException(
                    "Refresh token reuse detected");
        }

        refreshRegistryService.markUsed(refreshJti);

        return rotateTokens(session);
    }

    private AuthTokens rotateTokens(
            SessionRegistry session)
            throws Exception {

        String newRefreshJti =
                "RT-" + UUID.randomUUID();

        String accessToken =
                jwtGeneratorService.generateAccessToken(
                        session.getCustomerId(),
                        session.getDeviceId(),
                        session.getClientId(),
                        session.getSessionId()
                );

        String refreshToken =
                jwtGeneratorService.generateRefreshToken(
                        session.getCustomerId(),
                        session.getDeviceId(),
                        session.getClientId(),
                        session.getSessionId(),
                        newRefreshJti
                );

        refreshRegistryService.create(
                newRefreshJti,
                session.getSessionId()
        );

        session.setCurrentRefreshJti(newRefreshJti);

        sessionService.save(session);

        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .sessionId(session.getSessionId())
                .build();
    }

    private void revokeWholeSession(
            String sessionId) {

        SessionRegistry session =
                sessionService.load(sessionId);

        if (session == null) {
            return;
        }

        session.setStatus("REVOKED");

        sessionService.save(session);
    }
}