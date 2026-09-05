package com.isc.common.redis;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface RedisOperations {

    Optional<String> get(String key);

    void set(String key, String value);

    void set(String key, String value, Duration ttl);

    boolean setIfAbsent(String key, String value, Duration ttl);

    Map<String, String> entries(String key);

    void putAll(String key, Map<String, String> values);

    void put(String key, String field, String value);

    Optional<String> get(String key, String field);

    boolean hasKey(String key);

    boolean delete(String key);

    boolean expire(String key, Duration ttl);

    Long increment(String key);

    Set<String> keys(String pattern);

    boolean removeFromSortedSet(String key, String value);

    void addToSortedSet(String key, String value, double score);

    Set<String> rangeFromSortedSetByScore(String key, double minimum, double maximum);

    <T> T executeScript(
            String script,
            Class<T> resultType,
            List<String> keys,
            String... arguments);
}
