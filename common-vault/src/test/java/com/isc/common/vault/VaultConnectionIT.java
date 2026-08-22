package com.isc.common.vault;

import com.isc.common.vault.config.VaultProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.support.VaultResponse;
import org.springframework.vault.support.Versioned;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VaultConnectionIT {

    private static VaultTemplate vaultTemplate;
    private static VaultProperties properties;

    private static final String JWT_POLICY_PATH =
            env("VAULT_TEST_JWT_POLICY_PATH", "bank-jwt");

    private static final String TRANSIT_KEY =
            env("VAULT_TEST_TRANSIT_KEY_NAME", "bank-jwt");

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    @BeforeAll
    static void connectToVault() {
        String uriValue =
                env("VAULT_TEST_URI", "http://localhost:8200");

        String token = System.getenv("VAULT_TEST_TOKEN");

        assertNotNull(
                token,
                "VAULT_TEST_TOKEN must be configured"
        );

        URI uri = URI.create(uriValue);

        VaultEndpoint endpoint =
                VaultEndpoint.create(uri.getHost(), uri.getPort());

        endpoint.setScheme(uri.getScheme());

        vaultTemplate = new VaultTemplate(
                endpoint,
                new TokenAuthentication(token)
        );

        properties = new VaultProperties();
        properties.setEnabled(true);

        properties.getKv().setEnabled(true);
        properties.getKv().setMount(
                env("VAULT_TEST_KV_MOUNT", "key-management")
        );

        properties.getTransit().setEnabled(true);
        properties.getTransit().setMount(
                env("VAULT_TEST_TRANSIT_MOUNT", "transit")
        );
    }

    @Test
    void shouldConnectToVault() {
        VaultResponse response =
                vaultTemplate.read("sys/health");

        assertNotNull(response);
        assertNotNull(response.getData());
    }

    @Test
    void shouldHaveJwtPolicyInKv() {
        VaultVersionedKeyValueOperations kv =
                vaultTemplate.opsForVersionedKeyValue(
                        properties.getKv().getMount()
                );

        Versioned<Map<String, Object>> response =
                kv.get(JWT_POLICY_PATH);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());
    }

    @Test
    void shouldHaveExpectedJwtPolicyStructure() {
        VaultVersionedKeyValueOperations kv =
                vaultTemplate.opsForVersionedKeyValue(
                        properties.getKv().getMount()
                );

        Versioned<Map<String, Object>> response =
                kv.get(JWT_POLICY_PATH);

        assertNotNull(response);

        Map<String, Object> policy =
                response.getData();

        assertNotNull(policy);

        assertTrue(policy.containsKey("active"));
        assertTrue(policy.containsKey("inactive"));
        assertTrue(policy.containsKey("next"));
        assertTrue(policy.containsKey("retired"));

        assertInstanceOf(Map.class, policy.get("active"));
        assertInstanceOf(Map.class, policy.get("inactive"));
        assertInstanceOf(Map.class, policy.get("next"));
        assertInstanceOf(java.util.List.class, policy.get("retired"));
    }

    @Test
    void shouldHaveJwtTransitKey() {
        String path =
                properties.getTransit().getMount()
                        + "/keys/" + TRANSIT_KEY;

        VaultResponse response =
                vaultTemplate.read(path);

        assertNotNull(response);
        assertNotNull(response.getData());

        Object keys = response.getData().get("keys");

        assertInstanceOf(Map.class, keys);
        assertFalse(((Map<?, ?>) keys).isEmpty());
    }

    @Test
    void shouldHavePublicKeyForLatestJwtVersion() {
        String path =
                properties.getTransit().getMount()
                        + "/keys/" + TRANSIT_KEY;

        VaultResponse response =
                vaultTemplate.read(path);

        assertNotNull(response);

        Number latestVersion =
                (Number) response.getData()
                        .get("latest_version");

        assertNotNull(latestVersion);
        assertTrue(latestVersion.intValue() > 0);

        @SuppressWarnings("unchecked")
        Map<String, Object> keys =
                (Map<String, Object>)
                        response.getData().get("keys");

        assertNotNull(keys);

        Map<?, ?> latestKey =
                (Map<?, ?>)
                        keys.get(
                                String.valueOf(
                                        latestVersion.intValue()
                                )
                        );

        assertNotNull(latestKey);

        Object publicKey =
                latestKey.get("public_key");

        assertNotNull(publicKey);
        assertFalse(publicKey.toString().isBlank());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHaveTransitKeyForActivePolicyVersion() {
        VaultVersionedKeyValueOperations kv =
                vaultTemplate.opsForVersionedKeyValue(
                        properties.getKv().getMount()
                );

        Versioned<Map<String, Object>> policyResponse =
                kv.get(JWT_POLICY_PATH);

        assertNotNull(policyResponse);

        Map<String, Object> active =
                (Map<String, Object>)
                        policyResponse.getData().get("active");

        assertNotNull(active);

        Number activeVersion =
                (Number) active.get("vkv");

        assertNotNull(activeVersion);

        VaultResponse transitResponse =
                vaultTemplate.read(
                        properties.getTransit().getMount()
                                + "/keys/" + TRANSIT_KEY
                );

        assertNotNull(transitResponse);

        Map<String, Object> keys =
                (Map<String, Object>)
                        transitResponse.getData().get("keys");

        assertNotNull(keys);

        assertTrue(
                keys.containsKey(
                        String.valueOf(activeVersion.intValue())
                ),
                "Active policy version "
                        + activeVersion
                        + " does not exist in Transit"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHaveTransitKeyForEveryPolicyVersion() {
        VaultVersionedKeyValueOperations kv =
                vaultTemplate.opsForVersionedKeyValue(
                        properties.getKv().getMount()
                );

        Versioned<Map<String, Object>> policyResponse =
                kv.get(JWT_POLICY_PATH);

        assertNotNull(policyResponse);

        Map<String, Object> policy =
                policyResponse.getData();

        VaultResponse transitResponse =
                vaultTemplate.read(
                        properties.getTransit().getMount()
                                + "/keys/" + TRANSIT_KEY
                );

        assertNotNull(transitResponse);

        Map<String, Object> transitData =
                transitResponse.getData();

        Map<String, Object> transitKeys =
                (Map<String, Object>)
                        transitData.get("keys");

        assertNotNull(transitKeys);

        assertPolicyVersionExists(
                policy,
                "active",
                transitKeys
        );

        assertPolicyVersionExists(
                policy,
                "inactive",
                transitKeys
        );

        assertPolicyVersionExists(
                policy,
                "next",
                transitKeys
        );

        Object retired = policy.get("retired");

        assertInstanceOf(java.util.List.class, retired);

        for (Object item :
                (java.util.List<?>) retired) {

            assertInstanceOf(Map.class, item);

            Map<String, Object> retiredKey =
                    (Map<String, Object>) item;

            assertVersionExists(
                    retiredKey,
                    "retired",
                    transitKeys
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertPolicyVersionExists(
            Map<String, Object> policy,
            String state,
            Map<String, Object> transitKeys) {

        Object value = policy.get(state);

        assertNotNull(
                value,
                "Policy state '" + state + "' is missing"
        );

        assertInstanceOf(
                Map.class,
                value,
                "Policy state '" + state + "' must be an object"
        );

        assertVersionExists(
                (Map<String, Object>) value,
                state,
                transitKeys
        );
    }

    private static void assertVersionExists(
            Map<String, Object> keyInfo,
            String state,
            Map<String, Object> transitKeys) {

        Object vkv = keyInfo.get("vkv");

        assertNotNull(
                vkv,
                "vkv is missing from policy state " + state
        );

        assertInstanceOf(
                Number.class,
                vkv,
                "vkv must be numeric in policy state " + state
        );

        int version = ((Number) vkv).intValue();

        assertTrue(
                version > 0,
                "vkv must be positive in policy state " + state
        );

        assertTrue(
                transitKeys.containsKey(String.valueOf(version)),
                "Transit key version " + version
                        + " referenced by policy state '"
                        + state + "' does not exist"
        );
    }
}
