package com.isc.common.security.vault;

import com.isc.common.security.config.SecurityProperties;
import com.isc.common.security.exception.JwtPublicKeyNotFoundException;
import com.isc.common.vault.jwt.VaultJwtKeyStore;
import com.isc.common.vault.kv.VaultKeyValueService;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SecurityVaultPublicKeyProviderImpl implements SecurityVaultPublicKeyProvider {
    private final VaultJwtKeyStore keyStore;
    private final VaultKeyValueService keyValueService;
    private final SecurityProperties properties;

    public SecurityVaultPublicKeyProviderImpl(
            VaultJwtKeyStore keyStore,
            VaultKeyValueService keyValueService,
            SecurityProperties properties) {
        this.keyStore = keyStore;
        this.keyValueService = keyValueService;
        this.properties = properties;
    }

    @Override
    public PublicKey getPublicKey(String kid) {
        Integer version = resolveVaultVersion(kid);
        String pem = keyStore.getPublicKey(properties.getJwt().getVaultKeyName(), version);
        return parseRsaPublicKey(pem);
    }

    @Override
    public void invalidate() {
        // Policy is deliberately read on every cache miss. The local key cache
        // is the only cache in this module; invalidating it is handled by the
        // caller through SecurityKeyCache.invalidateAll().
    }

    private Integer resolveVaultVersion(String kid) {
        String mount = properties.getJwt().getPolicyMount();
        Map<String, Object> policy = mount == null || mount.isBlank()
                ? keyValueService.read(properties.getJwt().getPolicyPath(), Map.class)
                : keyValueService.read(mount, properties.getJwt().getPolicyPath(), Map.class);

        if (policy == null || policy.isEmpty()) {
            throw new JwtPublicKeyNotFoundException(kid);
        }

        Object info = findKeyInfo(policy, kid);
        if (!(info instanceof Map<?, ?> keyInfo)) {
            throw new JwtPublicKeyNotFoundException(kid);
        }

        Object version = firstNonNull(keyInfo, "vkv", "vaultKeyVersion", "version");
        if (version instanceof Number n) return n.intValue();
        if (version != null) {
            try {
                return Integer.valueOf(version.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        throw new JwtPublicKeyNotFoundException(kid);
    }

    private Object findKeyInfo(Map<String, Object> policy, String kid) {
        for (String section : List.of("active", "inactive", "next")) {
            Object value = policy.get(section);
            if (matchesKid(value, kid)) return value;
        }
        Object retired = policy.get("retired");
        if (retired instanceof List<?> list) {
            for (Object value : list) if (matchesKid(value, kid)) return value;
        }
        return null;
    }

    private boolean matchesKid(Object value, String kid) {
        if (!(value instanceof Map<?, ?> map)) return false;
        Object valueKid = firstNonNull(map, "kid", "keyId");
        return Objects.equals(kid, valueKid == null ? null : valueKid.toString());
    }

    private Object firstNonNull(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) return value;
        }
        return null;
    }

    private PublicKey parseRsaPublicKey(String pem) {
        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException("Vault returned an empty JWT public key");
        }
        String base64 = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\s+", "");
        try {
            byte[] encoded = Base64.getDecoder().decode(base64);
            return KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse Vault JWT RSA public key", ex);
        }
    }
}
