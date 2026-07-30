package com.isc.tokenservice.vault;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VaultTransitJwtSigner {

    private final VaultTemplate vaultTemplate;

    public String sign(
            String signingInput,
            int keyVersion) {

        //--------------------------------
        // Vault requires BASE64
        //--------------------------------

        String input =
                Base64.getEncoder()
                        .encodeToString(
                                signingInput.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        //--------------------------------
        // Transit sign request
        //--------------------------------

        VaultResponse response =
                vaultTemplate.write(
                        "transit/sign/bank-jwt-v2",
                        Map.of(
                                "input", input,
                                "key_version", keyVersion,
                                "hash_algorithm", "sha2-256",
                                "signature_algorithm", "pkcs1v15",
                                "prehashed", false
                        )
                );

        //--------------------------------
        // vault:v1:BASE64
        //--------------------------------

        String vaultSignature =
                response.getData()
                        .get("signature")
                        .toString();

        //--------------------------------
        // extract BASE64 part
        //--------------------------------

        String base64Signature =
                vaultSignature.substring(
                        vaultSignature.lastIndexOf(":") + 1
                );

        //--------------------------------
        // BASE64 -> bytes
        //--------------------------------

        byte[] signatureBytes =
                Base64.getDecoder()
                        .decode(base64Signature);

        //--------------------------------
        // JWT requires BASE64URL
        //--------------------------------

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(signatureBytes);
    }
}