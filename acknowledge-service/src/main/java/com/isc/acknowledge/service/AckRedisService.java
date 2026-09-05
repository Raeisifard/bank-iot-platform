package com.isc.acknowledge.service;

import com.isc.common.constants.RedisKeys;
import com.isc.common.redis.RedisOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "common.redis", name = "enabled", havingValue = "true")
public class AckRedisService {

        private final RedisOperations redis;

        private static final String ACK_SCRIPT;

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

            ACK_SCRIPT = script;

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

        Long result = redis.executeScript(
                ACK_SCRIPT,
                Long.class,
                List.of(deliveryKey, RedisKeys.MESSAGE_RETRY),
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