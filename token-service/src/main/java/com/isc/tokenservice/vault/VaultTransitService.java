package com.isc.tokenservice.vault;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VaultTransitService {

    private final VaultTemplate vaultTemplate;

    @Value("${security.vault.transit.key-name}")
    private String keyName;

    //--------------------------------
    // read public keys
    //--------------------------------
    // IMPORTANT: the export "type" segment must be "public-key".
    // "signing-key" (previously used here) exports the PRIVATE
    // key material meant for external signing — never expose
    // that from a public/JWKS-facing endpoint.
    public Map<String, Object> readPublicKeys() {
        VaultResponse response =
                vaultTemplate.read("transit/export/public-key/" + keyName);

        if (response == null) {
            throw new IllegalStateException(
                    "Vault returned null response for public-key export of " + keyName);
        }

        return response.getData();
    }

    public int rotateKey() {
        vaultTemplate.write("transit/keys/" + keyName + "/rotate", Map.of());

        VaultResponse response = vaultTemplate.read("transit/keys/" + keyName);

        if (response == null || response.getData() == null) {
            throw new IllegalStateException(
                    "Vault returned no data after rotating key " + keyName);
        }

        Map<String, Object> data = response.getData();

        return Integer.parseInt(
                data.get("latest_version").toString()
        );
    }
}
