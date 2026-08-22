package com.isc.common.security.keymanagement;

import java.security.PublicKey;

public class KeyResolverImpl implements KeyResolver {
    private final PublicKeyProvider provider;

    public KeyResolverImpl(PublicKeyProvider provider) {
        this.provider = provider;
    }

    @Override
    public PublicKey resolve(String id) {
        return provider.getPublicKey(id);
    }
}
