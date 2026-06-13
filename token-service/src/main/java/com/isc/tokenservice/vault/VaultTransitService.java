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
    public Map<String, Object> readKeys() {
        VaultResponse response = vaultTemplate.read("transit/keys/" + keyName);
        return response.getData();
    }

    public int rotateKey() {
        vaultTemplate.write("transit/keys/" + keyName + "/rotate", Map.of());
        VaultResponse response = vaultTemplate.read("transit/keys/" + keyName);
        Map<String, Object> data =  response.getData();
        assert data != null;
        return Integer.parseInt(
                data.get("latest_version").toString()
        );
    }
}
