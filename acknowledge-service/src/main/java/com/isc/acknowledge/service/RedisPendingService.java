package com.isc.acknowledge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisPendingService {

    private final StringRedisTemplate redisTemplate;

    public void savePending(String messageId) {

        redisTemplate.opsForValue()
                .set("pending:" + messageId,
                        "PENDING",
                        Duration.ofSeconds(30));
    }

    public void removePending(String messageId) {
        redisTemplate.delete("pending:" + messageId);
    }
}
