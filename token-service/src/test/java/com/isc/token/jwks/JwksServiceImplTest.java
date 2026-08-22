package com.isc.token.jwks;

import com.isc.common.vault.jwt.VaultJwtKeyStore;
import com.isc.token.config.JwtKeyPolicyProperties;
import com.isc.token.keymanagement.JwtKeyPolicy;
import com.isc.token.keymanagement.JwtKeyPolicyServiceImpl;
import com.isc.token.keymanagement.JwtKeyVersion;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwksServiceImplTest {

    @Test
    void shouldPublishEligibleVersions() throws Exception {

        JwtKeyPolicy policy = new JwtKeyPolicy(
                "jwt-signing",
                "RS256",
                Duration.ofDays(30),
                Duration.ofDays(7),
                3);

        JwtKeyPolicyProperties properties =
                new JwtKeyPolicyProperties();

        VaultJwtKeyStore keyStore =
                mock(VaultJwtKeyStore.class);

        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        RSAPublicKey publicKey =
                (RSAPublicKey) keyPair.getPublic();

        String pem = toPem(publicKey);

        when(keyStore.getPublicKey(
                "bank-jwt-v2", 1))
                .thenReturn(pem);

        var policyService =
                new JwtKeyPolicyServiceImpl(
                        policy,
                        properties,
                        mock(
                                com.isc.common.vault.kv.VaultKeyValueService.class));

        // Use a small policy-service stub so this test only tests JWKS conversion.
        com.isc.token.keymanagement.JwtKeyPolicyService stubPolicy =
                new com.isc.token.keymanagement.JwtKeyPolicyService() {
                    @Override
                    public JwtKeyPolicy getPolicy() {
                        return policy;
                    }

                    @Override
                    public JwtKeyVersion getActiveVersion() {
                        return new JwtKeyVersion(
                                "jwt-signing-1",
                                1,
                                com.isc.token.keymanagement.JwtKeyStatus.ACTIVE,
                                java.time.Instant.now(),
                                java.time.Instant.now(),
                                null);
                    }

                    @Override
                    public java.util.List<JwtKeyVersion>
                    getPublishableVersions() {
                        return java.util.List.of(getActiveVersion());
                    }
                };

        JwkProvider provider =
                new JwkProviderImpl(
                        keyStore,
                        properties);

        JwksService service =
                new JwksServiceImpl(
                        stubPolicy,
                        provider);

        var keys = service.getJwks();

        assertEquals(1, keys.size());

        Map<String, Object> jwk =
                keys.getFirst();

        assertEquals("RSA", jwk.get("kty"));
        assertEquals("RS256", jwk.get("alg"));
        assertEquals("sig", jwk.get("use"));
        assertEquals("jwt-signing-1", jwk.get("kid"));
        assertNotNull(jwk.get("n"));
        assertEquals("AQAB", jwk.get("e"));
    }

    private static String toPem(RSAPublicKey key) {
        String encoded = java.util.Base64.getEncoder()
                .encodeToString(key.getEncoded());

        return "-----BEGIN PUBLIC KEY-----\n"
                + encoded
                + "\n-----END PUBLIC KEY-----";
    }
}
