package com.isc.token.keymanagement;

import com.isc.common.vault.kv.VaultKeyValueService;
import com.isc.token.config.JwtKeyPolicyProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtKeyPolicyServiceImpl implements JwtKeyPolicyService {

    private final JwtKeyPolicy policy;
    private final JwtKeyPolicyProperties properties;
    private final VaultKeyValueService keyValueService;

    public JwtKeyPolicyServiceImpl(
            JwtKeyPolicy policy,
            JwtKeyPolicyProperties properties,
            VaultKeyValueService keyValueService) {
        this.policy = policy;
        this.properties = properties;
        this.keyValueService = keyValueService;
    }

    @Override
    public JwtKeyPolicy getPolicy() {
        return policy;
    }

    @Override
    public JwtKeyVersion getActiveVersion() {
        Map<String, Object> state = readPolicy();
        JwtKeyVersion active = parseVersion(state.get("active"), JwtKeyStatus.ACTIVE);
        if (active == null) {
            throw new IllegalStateException(
                    "JWT key policy has no active key: "
                            + properties.getPolicyPath());
        }
        return active;
    }

    @Override
    public List<JwtKeyVersion> getPublishableVersions() {
        Map<String, Object> state = readPolicy();
        List<JwtKeyVersion> result = new ArrayList<>();

        addIfPresent(result, state.get("active"), JwtKeyStatus.ACTIVE);
        addIfPresent(result, state.get("inactive"), JwtKeyStatus.PUBLISHED);
        addIfPresent(result, state.get("next"), JwtKeyStatus.PUBLISHED);

        Object retired = state.get("retired");
        if (retired instanceof List<?> list) {
            for (Object item : list) {
                addIfPresent(result, item, JwtKeyStatus.RETIRED);
            }
        }

        return result.stream()
                .filter(v -> v != null)
                .sorted(Comparator.comparing(
                        JwtKeyVersion::activatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(policy.maxPublishedVersions())
                .toList();
    }

    private Map<String, Object> readPolicy() {
        Map<String, Object> result;

        if (properties.getPolicyMount() == null
                || properties.getPolicyMount().isBlank()) {
            result = keyValueService.read(
                    properties.getPolicyPath(),
                    Map.class);
        } else {
            result = keyValueService.read(
                    properties.getPolicyMount(),
                    properties.getPolicyPath(),
                    Map.class);
        }

        if (result == null || result.isEmpty()) {
            throw new IllegalStateException(
                    "JWT key policy was not found in Vault KV: "
                            + properties.getPolicyPath());
        }

        return new LinkedHashMap<>(result);
    }

    private void addIfPresent(
            List<JwtKeyVersion> result,
            Object value,
            JwtKeyStatus defaultStatus) {

        JwtKeyVersion version = parseVersion(value, defaultStatus);
        if (version != null) {
            result.add(version);
        }
    }

    private JwtKeyVersion parseVersion(
            Object value,
            JwtKeyStatus defaultStatus) {

        if (!(value instanceof Map<?, ?> map)) {
            return null;
        }

        String keyId = stringValue(map, "kid", "keyId");
        Long version = longValue(map, "vkv", "vaultKeyVersion", "version");

        if (keyId == null || version == null || version <= 0) {
            return null;
        }

        Instant createdAt = instantValue(map, "createdAt", "created_at");
        Instant activatedAt = instantValue(
                map, "activatedAt", "activated_at");
        Instant retiredAt = instantValue(
                map, "retiredAt", "retired_at");

        JwtKeyStatus status = statusValue(map, defaultStatus);

        return new JwtKeyVersion(
                keyId,
                version,
                status,
                createdAt,
                activatedAt,
                retiredAt);
    }

    private String stringValue(
            Map<?, ?> map,
            String... names) {
        for (String name : names) {
            Object value = map.get(name);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }

    private Long longValue(
            Map<?, ?> map,
            String... names) {
        for (String name : names) {
            Object value = map.get(name);
            if (value instanceof Number n) {
                return n.longValue();
            }
            if (value != null) {
                try {
                    return Long.parseLong(value.toString());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    private Instant instantValue(
            Map<?, ?> map,
            String... names) {
        String value = stringValue(map, names);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private JwtKeyStatus statusValue(
            Map<?, ?> map,
            JwtKeyStatus defaultStatus) {
        String value = stringValue(map, "status");
        if (value == null) {
            return defaultStatus;
        }
        try {
            return JwtKeyStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return defaultStatus;
        }
    }
}
