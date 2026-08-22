package com.isc.token.jwks;

import com.isc.common.vault.jwt.VaultJwtKeyStore;
import com.isc.token.config.JwtKeyPolicyProperties;
import com.isc.token.keymanagement.JwtKeyVersion;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class JwkProviderImpl implements JwkProvider {

    private final VaultJwtKeyStore keyStore;
    private final JwtKeyPolicyProperties properties;

    public JwkProviderImpl(
            VaultJwtKeyStore keyStore,
            JwtKeyPolicyProperties properties) {
        this.keyStore = keyStore;
        this.properties = properties;
    }

    @Override
    public Map<String, Object> toJwk(JwtKeyVersion version) {
        if (version == null) {
            throw new IllegalArgumentException("JWT key version must not be null");
        }

        if (version.version() <= 0
                || version.version() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid Vault key version: " + version.version());
        }

        String pem = keyStore.getPublicKey(
                properties.getKeyName(),
                (int) version.version());

        RSAPublicKey publicKey = parseRsaPublicKey(pem);

        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("alg", properties.getAlgorithm());
        jwk.put("kid", version.keyId());
        jwk.put("n", encode(publicKey.getModulus()));
        jwk.put("e", encode(publicKey.getPublicExponent()));

        return jwk;
    }

    private RSAPublicKey parseRsaPublicKey(String pem) {
        if (pem == null || pem.isBlank()) {
            throw new IllegalStateException(
                    "Vault returned an empty JWT public key");
        }

        String base64 = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        try {
            byte[] encoded = Base64.getDecoder().decode(base64);

            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(encoded));

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to parse Vault JWT RSA public key", ex);
        }
    }

    private String encode(BigInteger value) {
        byte[] bytes = value.toByteArray();

        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] unsigned = new byte[bytes.length - 1];
            System.arraycopy(
                    bytes,
                    1,
                    unsigned,
                    0,
                    unsigned.length);
            bytes = unsigned;
        }

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
