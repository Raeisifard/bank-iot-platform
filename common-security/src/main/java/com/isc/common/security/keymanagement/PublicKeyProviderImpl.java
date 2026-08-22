package com.isc.common.security.keymanagement;

import com.isc.common.security.cache.SecurityKeyCache;
import com.isc.common.security.vault.SecurityVaultPublicKeyProvider;

import java.security.PublicKey;

public class PublicKeyProviderImpl implements PublicKeyProvider {
    private final SecurityKeyCache cache;
    private final SecurityVaultPublicKeyProvider vaultProvider;

    public PublicKeyProviderImpl(SecurityKeyCache cache, SecurityVaultPublicKeyProvider vaultProvider) {
        this.cache = cache;
        this.vaultProvider = vaultProvider;
    }

    @Override
    public PublicKey getPublicKey(String kid) {
        if (kid == null || kid.isBlank()) {
            throw new IllegalArgumentException("JWT kid must not be blank");
        }
        PublicKey cached = cache.get(kid);
        if (cached != null) return cached;
        PublicKey loaded = vaultProvider.getPublicKey(kid);
        cache.put(kid, loaded);
        return loaded;
    }
}
