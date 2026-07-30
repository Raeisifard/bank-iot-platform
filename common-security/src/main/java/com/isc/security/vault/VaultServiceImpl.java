package com.isc.security.vault;

import com.isc.security.config.VaultJwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {

    private final VaultTemplate vaultTemplate;
    private final VaultJwtProperties properties;

    @Override
    public PublicKey getPublicKey(String kid) {

        String path =
                properties.getPath() + "/" + kid;

        VaultResponse response =
                vaultTemplate.read(path);

        if (response == null || response.getData() == null) {
            throw new IllegalArgumentException(
                    "Public key not found. kid=" + kid);
        }

        Map<String, Object> data =
                response.getData();

        String publicKeyPem =
                (String) data.get("publicKey");

        return toPublicKey(publicKeyPem);
    }

    private PublicKey toPublicKey(String pem) {

        try {

            String key =
                    pem.replace(
                                    "-----BEGIN PUBLIC KEY-----", "")
                            .replace(
                                    "-----END PUBLIC KEY-----", "")
                            .replaceAll("\\s+", "");

            byte[] decoded =
                    Base64.getDecoder().decode(key);

            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA")
                    .generatePublic(spec);

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Cannot parse public key", ex);
        }
    }
}