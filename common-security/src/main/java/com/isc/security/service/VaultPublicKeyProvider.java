package com.isc.security.service;

import com.isc.security.vault.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.PublicKey;

@Service
@RequiredArgsConstructor
public class VaultPublicKeyProvider
        implements PublicKeyProvider {

    private final VaultService vaultService;

    @Override
    public PublicKey getPublicKey(String kid) {

        return vaultService.getPublicKey(kid);
    }
}
