package com.isc.acknowledge.repository;

import com.isc.common.constants.RedisKeys;
import com.isc.common.redis.RedisOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "common.redis", name = "enabled", havingValue = "true")
public class DeliveryRedisRepository {

    private final RedisOperations redis;

    private String key(String messageId) {
        return RedisKeys.MESSAGE_DELIVERY + messageId;
    }

    public Optional<Map<String, String>> find(String messageId) {

        Map<String, String> values = redis.entries(key(messageId));

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

        return redis.get(key(messageId), field).orElse(null);
    }

    public void put(
            String messageId,
            String field,
            String value) {

        redis.put(key(messageId), field, value);
    }

    public void removeFromRetryQueue(
            String messageId) {

        redis.removeFromSortedSet(RedisKeys.MESSAGE_RETRY, messageId);
    }
}