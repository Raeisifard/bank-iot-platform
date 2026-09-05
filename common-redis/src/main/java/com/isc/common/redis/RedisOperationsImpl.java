package com.isc.common.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class RedisOperationsImpl implements RedisOperations {

    private final StringRedisTemplate redis;

    RedisOperationsImpl(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(
                redis.opsForValue().get(key)
        );
    }

    @Override
    public void set(String key, String value) {
        redis.opsForValue().set(key, value);
    }

    @Override
    public void set(
            String key,
            String value,
            Duration ttl) {

        redis.opsForValue().set(key, value, ttl);
    }

    @Override
    public boolean setIfAbsent(
            String key,
            String value,
            Duration ttl) {

        Boolean result =
                redis.opsForValue().setIfAbsent(
                        key,
                        value,
                        ttl
                );

        return Boolean.TRUE.equals(result);
    }

    @Override
    public Map<String, String> entries(String key) {

        Map<Object, Object> entries =
                redis.opsForHash().entries(key);

        if (entries == null || entries.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> result =
                new LinkedHashMap<>(entries.size());

        entries.forEach((field, value) -> {
            if (field != null) {
                result.put(
                        field.toString(),
                        value == null ? null : value.toString()
                );
            }
        });

        return result;
    }

    @Override
    public void putAll(
            String key,
            Map<String, String> values) {

        if (values == null || values.isEmpty()) {
            return;
        }

        Map<Object, Object> hashValues =
                new LinkedHashMap<>(values.size());

        values.forEach(hashValues::put);

        redis.opsForHash().putAll(
                key,
                hashValues
        );
    }

    @Override
    public void put(
            String key,
            String field,
            String value) {

        redis.opsForHash().put(
                key,
                field,
                value
        );
    }

    @Override
    public Optional<String> get(
            String key,
            String field) {

        Object value =
                redis.opsForHash().get(key, field);

        return Optional.ofNullable(
                value == null ? null : value.toString()
        );
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(
                redis.hasKey(key)
        );
    }

    @Override
    public boolean delete(String key) {
        return Boolean.TRUE.equals(
                redis.delete(key)
        );
    }

    @Override
    public boolean expire(
            String key,
            Duration ttl) {

        return Boolean.TRUE.equals(
                redis.expire(key, ttl)
        );
    }

    @Override
    public Long increment(String key) {
        return redis.opsForValue().increment(key);
    }

    @Override
    public Set<String> keys(String pattern) {

        Set<String> result =
                redis.keys(pattern);

        return result == null
                ? Collections.emptySet()
                : result;
    }

    @Override
    public boolean removeFromSortedSet(String key, String value) {
        return Boolean.TRUE.equals(redis.opsForZSet().remove(key, value));
    }

    @Override
    public void addToSortedSet(String key, String value, double score) {
        redis.opsForZSet().add(key, value, score);
    }

    @Override
    public Set<String> rangeFromSortedSetByScore(
            String key,
            double minimum,
            double maximum) {

        Set<String> result = redis.opsForZSet().rangeByScore(key, minimum, maximum);
        return result == null ? Collections.emptySet() : result;
    }

    @Override
    public <T> T executeScript(
            String script,
            Class<T> resultType,
            List<String> keys,
            String... arguments) {

        return redis.execute(
                new DefaultRedisScript<>(script, resultType),
                keys,
                (Object[]) arguments
        );
    }
}