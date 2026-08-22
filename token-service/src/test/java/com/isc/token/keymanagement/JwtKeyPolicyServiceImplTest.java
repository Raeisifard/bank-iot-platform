package com.isc.token.keymanagement;

import com.isc.common.vault.kv.VaultKeyValueService;
import com.isc.token.config.JwtKeyPolicyProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtKeyPolicyServiceImplTest {

    @Test
    void activeVersionShouldBeReadFromVaultPolicy() {

        JwtKeyPolicy policy = new JwtKeyPolicy(
                "jwt-signing",
                "RS256",
                Duration.ofDays(30),
                Duration.ofDays(7),
                3);

        JwtKeyPolicyProperties properties =
                new JwtKeyPolicyProperties();

        VaultKeyValueService kv =
                mock(VaultKeyValueService.class);

        when(kv.read("jwt/key-policy", Map.class))
                .thenReturn(Map.of(
                        "active", Map.of(
                                "kid", "jwt-signing-7",
                                "vkv", 7
                        )
                ));

        JwtKeyPolicyServiceImpl service =
                new JwtKeyPolicyServiceImpl(
                        policy,
                        properties,
                        kv);

        JwtKeyVersion version =
                service.getActiveVersion();

        assertEquals("jwt-signing-7", version.keyId());
        assertEquals(7, version.version());
        assertEquals(JwtKeyStatus.ACTIVE, version.status());
    }
}
