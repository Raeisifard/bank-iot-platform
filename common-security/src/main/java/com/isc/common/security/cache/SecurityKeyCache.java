package com.isc.common.security.cache;

import java.security.PublicKey;

public interface SecurityKeyCache {
    PublicKey get(String keyId);

    void put(String keyId, PublicKey key);

    void invalidate(String keyId);

    void invalidateAll();
}
