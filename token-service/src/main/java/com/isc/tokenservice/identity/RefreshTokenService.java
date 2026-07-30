package com.isc.tokenservice.identity;

import com.isc.tokenservice.config.JwtProperties;
import com.isc.tokenservice.dto.RefreshRotationResult;
import com.isc.tokenservice.dto.RefreshTokenInfo;
import com.isc.tokenservice.enums.RefreshTokenStatus;
import com.isc.tokenservice.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.isc.common.constants.RedisKeys.*;
import static com.isc.tokenservice.constant.RedisRefreshFields.*;
import static com.isc.tokenservice.util.HashUtils.sha256;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redis;

    /**
     * Create refresh token for session
     */
    public String create(String customerId, String deviceId, String clientId, String sessionId) {

        String refreshToken = UUID.randomUUID().toString();
        //String refreshTokenId = UUID.randomUUID().toString();// = RTI
        String refreshTokenHash = sha256(refreshToken);
        String key = buildRefreshKey(refreshTokenHash);
        Map<String, String> values = new HashMap<>();
        //values.put(REFRESH_TOKEN_ID, refreshTokenId);
        values.put(SESSION_ID, sessionId);
        values.put(CUSTOMER_ID, customerId);
        values.put(DEVICE_ID, deviceId);
        values.put(CLIENT_ID, clientId);
        values.put(REFRESH_TOKEN_HASH, refreshTokenHash);
        Instant now = Instant.now();
        values.put(CREATED_AT, now.toString());
        values.put(LAST_ROTATE_AT, now.toString());
        values.put(EXPIRE_AT, now.plus(jwtProperties.getRefreshTokenTtl())
                .plus(jwtProperties.getRefreshTokenAuditTtl()).toString());
        values.put(STATUS, RefreshTokenStatus.ACTIVE.name());
        redis.opsForHash().putAll(key, values);
        redis.expire(key, jwtProperties.getRefreshTokenTtl().plus(jwtProperties.getRefreshTokenAuditTtl()));

        /*
         * session -> refresh token
         */
        redis.opsForValue().set(buildSessionRefreshKey(sessionId), refreshTokenHash, jwtProperties.getRefreshTokenTtl());
        return refreshToken;
    }

    /**
     * Validate refresh token
     */
    public RefreshTokenInfo validate(String refreshTokenId) {

        RefreshTokenInfo token = get(refreshTokenId);
        if (token == null) {
            throw new InvalidRefreshTokenException("Refresh token id not found");
        }
        if (token.getStatus() != RefreshTokenStatus.ACTIVE) {
            throw new InvalidRefreshTokenException("Refresh token revoked");
        }
        if (token.getExpireAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }
        if (token.getSessionId() == null || token.getSessionId().isBlank()) {
            throw new IllegalStateException("Refresh token corrupted: sessionId missing");
        }
        if (token.getRefreshTokenHash() == null || token.getRefreshTokenHash().isBlank()) {
            throw new IllegalStateException("Refresh token corrupted: refreshTokenHash missing");
        }
        return token;
    }

    /**
     * Get refresh token
     */
    public RefreshTokenInfo get(String refreshTokenId) {

        Map<Object, Object> map = redis.opsForHash().entries(buildRefreshKey(refreshTokenId));
        if (map.isEmpty()) {
            return null;
        }
        map.get(REVOKE_AT);
        return RefreshTokenInfo.builder()
                //.refreshTokenId((String) map.get(REFRESH_TOKEN_ID))
                .sessionId((String) map.get(SESSION_ID))
                .customerId((String) map.get(CUSTOMER_ID))
                .deviceId((String) map.get(DEVICE_ID))
                .clientId((String) map.get(CLIENT_ID))
                .refreshTokenHash((String) map.get(REFRESH_TOKEN_HASH))
                .createdAt(Instant.parse((String) map.get(CREATED_AT)))
                .lastRotatedAt(Instant.parse((String) map.get(LAST_ROTATE_AT)))
                .expireAt(Instant.parse((String) map.get(EXPIRE_AT)))
                .status(RefreshTokenStatus.valueOf((String) map.get(STATUS)))
                .revokedAt(map.get(REVOKE_AT) != null ? Instant.parse((String) map.get(REVOKE_AT)) : null)
                .build();
    }

    /**
     * Revoke refresh token
     */
    public void revoke(String refreshTokenId) {

        RefreshTokenInfo token = get(refreshTokenId);
        if (token == null) {
            return;
        }
        redis.opsForHash().put(buildRefreshKey(refreshTokenId), STATUS, RefreshTokenStatus.REVOKED.name());
        redis.delete(buildSessionRefreshKey(token.getSessionId()));
    }

    /**
     * Revoke refresh token by session
     */
    public void revokeBySession(String sessionId) {

        String refreshTokenId = redis.opsForValue().get(buildSessionRefreshKey(sessionId));
        if (refreshTokenId == null) {
            return;
        }
        revoke(refreshTokenId);
    }

    /**
     * Refresh Token Rotation
     */
    public RefreshRotationResult rotate(RefreshTokenInfo refreshTokenInfo) {

        //RefreshTokenInfo token = validate(refreshTokenHash);
        revoke(refreshTokenInfo.getRefreshTokenHash());

        String newRefreshToken = create(
                refreshTokenInfo.getCustomerId(),
                refreshTokenInfo.getDeviceId(),
                refreshTokenInfo.getClientId(),
                refreshTokenInfo.getSessionId()
        );

        //sessionService.refreshSession(token.getSessionId());
        //sessionService.touchSession(token.getSessionId());

        return RefreshRotationResult.builder()
                .refreshToken(newRefreshToken)
                .customerId(refreshTokenInfo.getCustomerId())
                .deviceId(refreshTokenInfo.getDeviceId())
                .clientId(refreshTokenInfo.getClientId())
                .sessionId(refreshTokenInfo.getSessionId())
                .build();
    }

    private String buildRefreshKey(String refreshTokenId) {

        return REFRESH_TOKEN + refreshTokenId;
    }

    private String buildSessionRefreshKey(String sessionId) {

        return SESSION_REFRESH + sessionId;
    }
}