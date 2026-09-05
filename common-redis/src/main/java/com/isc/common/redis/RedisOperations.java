package com.isc.common.redis;

import java.time.Duration;
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
}
