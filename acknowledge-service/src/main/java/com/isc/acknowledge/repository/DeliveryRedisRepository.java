package com.isc.acknowledge.repository;

import com.isc.common.constants.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeliveryRedisRepository {

    private final StringRedisTemplate redis;

    private String key(String messageId) {
        return RedisKeys.MESSAGE_DELIVERY + messageId;
    }

    public Optional<Map<Object, Object>> find(String messageId) {

        Map<Object, Object> values =
                redis.opsForHash().entries(key(messageId));

        if (values.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(values);
    }

    public boolean exists(String messageId) {
        return redis.hasKey(key(messageId));
    }

    public String get(
            String messageId,
            String field) {

        Object value =
                redis.opsForHash().get(
                        key(messageId),
                        field
                );

        return value == null
                ? null
                : value.toString();
    }

    public void put(
            String messageId,
            String field,
            String value) {

        redis.opsForHash().put(
                key(messageId),
                field,
                value
        );
    }

    public void removeFromRetryQueue(
            String messageId) {

        redis.opsForZSet().remove(
                RedisKeys.MESSAGE_RETRY,
                messageId
        );
    }
}