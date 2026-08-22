package com.isc.common.vault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.common.vault.config.VaultProperties;
import com.isc.common.vault.kv.VaultKeyValueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.support.Versioned;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VaultKeyValueServiceTest {

    private VaultTemplate vaultTemplate;
    private VaultProperties properties;
    private VaultVersionedKeyValueOperations kvOperations;
    private VaultKeyValueServiceImpl service;

    @BeforeEach
    void setUp() {
        vaultTemplate = mock(VaultTemplate.class);
        kvOperations = mock(VaultVersionedKeyValueOperations.class);

        properties = new VaultProperties();
        properties.setEnabled(true);
        properties.getKv().setEnabled(true);
        properties.getKv().setMount("key-management");

        service = new VaultKeyValueServiceImpl(
                vaultTemplate,
                properties,
                new ObjectMapper()
        );

        when(vaultTemplate.opsForVersionedKeyValue("key-management"))
                .thenReturn(kvOperations);
    }

    @Test
    void shouldReadExistingKey() {
        Map<String, Object> data = Map.of(
                "active", Map.of(
                        "kid", "key-2026-02",
                        "vkv", 2
                )
        );

        when(kvOperations.get("bank-jwt"))
                .thenReturn(Versioned.create(data));

        TestPolicy result =
                service.read("bank-jwt", TestPolicy.class);

        assertNotNull(result);
        assertNotNull(result.active());
        assertEquals("key-2026-02", result.active().kid());
        assertEquals(2, result.active().vkv());
        verify(kvOperations).get("bank-jwt");
    }

    @Test
    void shouldReturnNullWhenKeyDoesNotExist() {
        when(kvOperations.get("bank-jwt")).thenReturn(null);

        assertNull(
                service.read("bank-jwt", TestPolicy.class)
        );
    }

    @Test
    void shouldWriteKey() {
        TestPolicy policy =
                new TestPolicy(new TestKey("key-2026-02", 2));

        service.write("bank-jwt", policy);

        verify(kvOperations).put(
                eq("bank-jwt"),
                anyMap()
        );
    }

    record TestPolicy(TestKey active) {}
    record TestKey(String kid, int vkv) {}
}
