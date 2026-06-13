package com.isc.tokenservice.controller;

import com.isc.tokenservice.dto.JwtKeyPolicy;
import com.isc.tokenservice.vault.JwtKeyPolicyService;
import com.isc.tokenservice.vault.VaultTransitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
public class JwksController {

    private final VaultTransitService vault;
    private final JwtKeyPolicyService policyService;

    @GetMapping("/jwks.json")
    public Map<String, Object> jwks() {

        //--------------------------------
        // load cached policy
        //--------------------------------

        JwtKeyPolicy policy =
                policyService.getCachedPolicy();

        //--------------------------------
        // build allowed versions map
        //--------------------------------

        Map<Integer, String> allowedKids =
                new HashMap<>();

        //--------------------------------
        // active
        //--------------------------------

        if (policy.getActive() != null) {

            allowedKids.put(
                    policy.getActive().getVkv(),
                    policy.getActive().getKid()
            );
        }

        //--------------------------------
        // next
        //--------------------------------

        if (policy.getNext() != null) {

            allowedKids.put(
                    policy.getNext().getVkv(),
                    policy.getNext().getKid()
            );
        }

        //--------------------------------
        // inactive
        //--------------------------------

        if (policy.getInactive() != null) {

            allowedKids.put(
                    policy.getInactive().getVkv(),
                    policy.getInactive().getKid()
            );
        }

        //--------------------------------
        // vault keys
        //--------------------------------

        Map<String, Object> response = vault.readKeys();

        Map<String, Object> keys = (Map<String, Object>) response.get("keys");

        List<Map<String, Object>> result = new ArrayList<>();

        //--------------------------------
        // only active + next + inactive
        //--------------------------------

        keys.forEach((version, value) -> {

            try {

                int vaultVersion = Integer.parseInt(version);

                //--------------------------------
                // skip non allowed versions
                //--------------------------------

                if (!allowedKids.containsKey(vaultVersion)) {
                    return;
                }

                //--------------------------------
                // mapped kid
                //--------------------------------

                String kid = allowedKids.get(vaultVersion);

                //--------------------------------
                // vault public key
                //--------------------------------

                Map<String, Object> keyData = (Map<String, Object>) value;

                String pem = (String) keyData.get("public_key");

                RSAPublicKey key = parsePem(pem);

                //--------------------------------
                // jwk
                //--------------------------------

                Map<String, Object> jwk =
                        new HashMap<>();

                jwk.put("kty", "RSA");
                jwk.put("kid", kid);

                jwk.put("alg", "RS256");
                jwk.put("use", "sig");

                jwk.put(
                        "n",
                        base64Url(
                                normalizeUnsigned(
                                        key.getModulus()
                                                .toByteArray()
                                )
                        )
                );

                jwk.put(
                        "e",
                        base64Url(
                                normalizeUnsigned(
                                        key.getPublicExponent()
                                                .toByteArray()
                                )
                        )
                );

                result.add(jwk);

            } catch (Exception ex) {

                throw new RuntimeException(ex);
            }
        });

        return Map.of("keys", result);
    }

    //--------------------------------
    // normalize unsigned bigint
    //--------------------------------

    private byte[] normalizeUnsigned(byte[] input) {

        if (input.length > 1 && input[0] == 0) {

            return Arrays.copyOfRange(
                    input,
                    1,
                    input.length
            );
        }

        return input;
    }

    //--------------------------------
    // PEM → RSA
    //--------------------------------

    private RSAPublicKey parsePem(String pem)
            throws Exception {

        pem = pem
                .replace(
                        "-----BEGIN PUBLIC KEY-----",
                        "")
                .replace(
                        "-----END PUBLIC KEY-----",
                        "")
                .replaceAll("\\s", "");

        byte[] bytes =
                Base64.getDecoder()
                        .decode(pem);

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(bytes);

        KeyFactory factory =
                KeyFactory.getInstance("RSA");

        return (RSAPublicKey)
                factory.generatePublic(spec);
    }

    //--------------------------------
    // base64url
    //--------------------------------

    private String base64Url(byte[] bytes) {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}