package com.isc.tokenservice.identity;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RevocationService {

    private final StringRedisTemplate redis;

    public void revoke(
            String jti,
            long expiresInSeconds) {

        redis.opsForValue().set(
                "revoked:" + jti,
                "1",
                Duration.ofSeconds(
                        expiresInSeconds));
    }

    public boolean isRevoked(String jti) {

        return redis.hasKey(
                "revoked:" + jti);
    }
}