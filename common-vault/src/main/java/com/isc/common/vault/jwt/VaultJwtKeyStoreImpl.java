package com.isc.common.vault.jwt;

import com.isc.common.vault.transit.VaultTransitService;
import org.springframework.stereotype.Service;

@Service
public class VaultJwtKeyStoreImpl implements VaultJwtKeyStore {

    private final VaultTransitService transitService;

    public VaultJwtKeyStoreImpl(VaultTransitService transitService) {
        this.transitService = transitService;
    }

    @Override
    public String getPublicKey(String keyName, Integer keyVersion) {
        return transitService.getPublicKey(keyName, keyVersion);
    }

    @Override
    public String sign(
            String keyName,
            String input,
            Integer keyVersion) {

        return transitService.sign(
                keyName,
                input,
                keyVersion
        );
    }
}
