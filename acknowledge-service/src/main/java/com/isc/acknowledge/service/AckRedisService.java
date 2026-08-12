package com.isc.acknowledge.service;

import com.isc.common.constants.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AckRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final DefaultRedisScript<Long> ACK_SCRIPT;

    static {
        try {
            var resource =
                    new ClassPathResource(
                            "redis/ack-delivery.lua"
                    );

            String script =
                    new String(
                            resource.getInputStream().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            ACK_SCRIPT =
                    new DefaultRedisScript<>(
                            script,
                            Long.class
                    );

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public AckResult acknowledge(
            String messageId,
            String clientId,
            String status,
            Instant ackAt) {

        String deliveryKey =
                RedisKeys.MESSAGE_DELIVERY + messageId;

        Long result =
                redisTemplate.execute(
                        ACK_SCRIPT,
                        List.of(
                                deliveryKey,
                                RedisKeys.MESSAGE_RETRY
                        ),
                        clientId,
                        status,
                        ackAt.toString()
                );

        return AckResult.from(result);
    }

    public enum AckResult {

        ACKED(1),
        DUPLICATE(2),
        NOT_FOUND(0),
        CLIENT_MISMATCH(-1);

        private final long code;

        AckResult(long code) {
            this.code = code;
        }

        public static AckResult from(Long value) {

            if (value == null) {
                throw new IllegalStateException(
                        "Redis ACK script returned null"
                );
            }

            for (AckResult result : values()) {
                if (result.code == value) {
                    return result;
                }
            }

            throw new IllegalStateException(
                    "Unknown Redis ACK result: " + value
            );
        }
    }
}