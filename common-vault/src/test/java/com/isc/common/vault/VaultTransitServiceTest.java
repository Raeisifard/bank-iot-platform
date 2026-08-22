package com.isc.common.vault;

import com.isc.common.vault.config.VaultProperties;
import com.isc.common.vault.transit.VaultTransitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VaultTransitServiceTest {

    private VaultTemplate vaultTemplate;
    private VaultProperties properties;
    private VaultTransitServiceImpl service;

    @BeforeEach
    void setUp() {
        vaultTemplate = mock(VaultTemplate.class);

        properties = new VaultProperties();
        properties.setEnabled(true);
        properties.getTransit().setEnabled(true);
        properties.getTransit().setMount("transit");

        service = new VaultTransitServiceImpl(
                vaultTemplate,
                properties
        );
    }

    @Test
    void shouldReturnPublicKeyForRequestedVersion() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of(
                "latest_version", 2,
                "keys", Map.of(
                        "1", Map.of("public_key", "OLD"),
                        "2", Map.of("public_key", "ACTIVE")
                )
        ));

        when(vaultTemplate.read("transit/keys/bank-jwt"))
                .thenReturn(response);

        assertEquals(
                "ACTIVE",
                service.getPublicKey("bank-jwt", 2)
        );
    }

    @Test
    void shouldReturnLatestPublicKeyWhenVersionIsNull() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of(
                "latest_version", 2,
                "keys", Map.of(
                        "1", Map.of("public_key", "OLD"),
                        "2", Map.of("public_key", "LATEST")
                )
        ));

        when(vaultTemplate.read("transit/keys/bank-jwt"))
                .thenReturn(response);

        assertEquals(
                "LATEST",
                service.getPublicKey("bank-jwt", null)
        );
    }

    @Test
    void shouldFailWhenRequestedVersionDoesNotExist() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of(
                "latest_version", 1,
                "keys", Map.of(
                        "1", Map.of("public_key", "OLD")
                )
        ));

        when(vaultTemplate.read("transit/keys/bank-jwt"))
                .thenReturn(response);

        assertThrows(
                IllegalStateException.class,
                () -> service.getPublicKey("bank-jwt", 2)
        );
    }

    @Test
    void shouldSignUsingRequestedKeyVersion() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of(
                "signature", "vault:v2:signature"
        ));

        when(vaultTemplate.write(
                eq("transit/sign/bank-jwt"),
                anyMap()
        )).thenReturn(response);

        assertEquals(
                "vault:v2:signature",
                service.sign("bank-jwt", "hello", 2)
        );

        verify(vaultTemplate).write(
                eq("transit/sign/bank-jwt"),
                anyMap()
        );
    }
}
