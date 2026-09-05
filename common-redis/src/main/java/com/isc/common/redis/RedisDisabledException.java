package com.isc.common.redis;

public class RedisDisabledException extends IllegalStateException {

    public RedisDisabledException() {
        super("Redis is disabled. Set common.redis.enabled=true to enable Redis access.");
    }
}
