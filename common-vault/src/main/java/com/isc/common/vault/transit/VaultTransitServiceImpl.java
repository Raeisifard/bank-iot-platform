package com.isc.common.vault.transit;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.isc.common.vault.config.VaultProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

@Service
@ConditionalOnBean(VaultTemplate.class)
public class VaultTransitServiceImpl implements VaultTransitService {

    private final VaultTemplate vaultTemplate;
    private final VaultProperties properties;

    public VaultTransitServiceImpl(
            VaultTemplate vaultTemplate,
            VaultProperties properties) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
    }

    @Override
    public String sign(String keyName, String input) {
        return sign(
                properties.getTransit().getMount(),
                keyName,
                input,
                null
        );
    }

    @Override
    public String sign(
            String keyName,
            String input,
            Integer keyVersion) {
        return sign(
                properties.getTransit().getMount(),
                keyName,
                input,
                keyVersion
        );
    }

    @Override
    public String sign(
            String mount,
            String keyName,
            String input,
            Integer keyVersion) {

        requireTransitEnabled();

        mount = normalizeMount(mount);

        Map<String, Object> request = new HashMap<>();
        request.put(
                "input",
                Base64.getEncoder().encodeToString(
                        input.getBytes(StandardCharsets.UTF_8)
                )
        );

        request.put(
                "hash_algorithm",
                "sha2-256"
        );

        request.put(
                "signature_algorithm",
                "pkcs1v15"
        );

        if (keyVersion != null) {
            request.put("key_version", keyVersion);
        }

        VaultResponse response = vaultTemplate.write(
                mount + "/sign/" + keyName,
                request
        );

        if (response == null || response.getData() == null) {
            throw new IllegalStateException(
                    "Vault Transit returned an empty signing response");
        }

        Object signature = response.getData().get("signature");

        if (signature == null) {
            throw new IllegalStateException(
                    "Vault Transit response does not contain signature");
        }

        return signature.toString();
    }

    @Override
    public String getPublicKey(
            String keyName,
            Integer keyVersion) {

        return getPublicKey(
                properties.getTransit().getMount(),
                keyName,
                keyVersion
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getPublicKey(
            String mount,
            String keyName,
            Integer keyVersion) {

        requireTransitEnabled();

        mount = normalizeMount(mount);

        VaultResponse response = vaultTemplate.read(
                mount + "/keys/" + keyName
        );

        if (response == null || response.getData() == null) {
            throw new IllegalStateException(
                    "Vault Transit returned an empty key response");
        }

        Object keysObject = response.getData().get("keys");

        if (!(keysObject instanceof Map<?, ?> keys)) {
            throw new IllegalStateException(
                    "Vault Transit response does not contain a valid keys map");
        }

        if (keys.isEmpty()) {
            throw new IllegalStateException(
                    "Vault Transit returned no key versions");
        }

        Integer selectedVersion = keyVersion;

        if (selectedVersion == null) {
            Object latestVersion = response.getData().get("latest_version");
            if (latestVersion instanceof Number number) {
                selectedVersion = number.intValue();
            }
        }

        if (selectedVersion == null) {
            throw new IllegalStateException(
                    "Vault Transit key version is not available");
        }

        Object versionObject = keys.get(String.valueOf(selectedVersion));

        if (versionObject == null) {
            versionObject = keys.get(selectedVersion);
        }

        if (!(versionObject instanceof Map<?, ?> versionData)) {
            throw new IllegalStateException(
                    "Vault Transit key version " + selectedVersion
                            + " was not found");
        }

        Object publicKey = versionData.get("public_key");

        if (publicKey == null) {
            throw new IllegalStateException(
                    "Vault Transit key version " + selectedVersion
                            + " does not contain public_key");
        }

        return publicKey.toString();
    }

    private void requireTransitEnabled() {
        if (!properties.getTransit().isEnabled()) {
            throw new IllegalStateException("Vault Transit is disabled");
        }
    }

    /**
     * Defends against a misconfigured mount value carrying a trailing
     * slash (e.g. "transit/" instead of "transit"), which would otherwise
     * produce a double-slash path such as "transit//sign/keyName".
     */
    private String normalizeMount(String mount) {
        if (mount == null) {
            return mount;
        }
        int end = mount.length();
        while (end > 0 && mount.charAt(end - 1) == '/') {
            end--;
        }
        return mount.substring(0, end);
    }
}
