package com.isc.common.security.cache;

import com.github.benmanes.caffeine.cache.Cache;

import java.security.PublicKey;

public class SecurityKeyCacheImpl implements SecurityKeyCache {
    private final Cache<String, PublicKey> cache;

    public SecurityKeyCacheImpl(Cache<String, PublicKey> cache) {
        this.cache = cache;
    }

    @Override
    public PublicKey get(String id) {
        return cache.getIfPresent(id);
    }

    @Override
    public void put(String id, PublicKey key) {
        cache.put(id, key);
    }

    @Override
    public void invalidate(String id) {
        cache.invalidate(id);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }
}
