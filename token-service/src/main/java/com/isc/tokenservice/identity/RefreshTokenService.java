package com.isc.tokenservice.identity;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redis;

    public String create(
            String customerId,
            String sessionId) {

        String token =
                UUID.randomUUID().toString();

        redis.opsForValue().set(
                "refresh:" + token,
                sessionId,
                Duration.ofDays(30));

        return token;
    }

    public String validate(String refreshToken) {

        String sessionId =
                redis.opsForValue()
                        .get("refresh:" + refreshToken);

        if (sessionId == null) {

            throw new RuntimeException(
                    "Invalid refresh token");
        }

        return sessionId;
    }

    public void revoke(String refreshToken) {

        redis.delete("refresh:" + refreshToken);
    }
}