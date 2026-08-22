package com.isc.token.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.common.vault.jwt.VaultJwtKeyStore;
import com.isc.token.config.JwtProperties;
import com.isc.token.config.TokenServiceProperties;
import com.isc.token.dto.TokenRequest;
import com.isc.token.exception.TokenGenerationException;
import com.isc.token.keymanagement.JwtKeyPolicyService;
import com.isc.token.keymanagement.JwtKeyVersion;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtGeneratorServiceImpl implements JwtGeneratorService {

    private final TokenServiceProperties tokenProperties;
    private final JwtProperties jwtProperties;
    private final JwtKeyPolicyService keyPolicyService;
    private final VaultJwtKeyStore keyStore;
    private final ObjectMapper objectMapper;

    public JwtGeneratorServiceImpl(
            TokenServiceProperties tokenProperties,
            JwtProperties jwtProperties,
            JwtKeyPolicyService keyPolicyService,
            VaultJwtKeyStore keyStore,
            ObjectMapper objectMapper) {
        this.tokenProperties = tokenProperties;
        this.jwtProperties = jwtProperties;
        this.keyPolicyService = keyPolicyService;
        this.keyStore = keyStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateAccessToken(TokenRequest request) {
        return generate(
                request,
                "access_token",
                tokenProperties.getAccessTokenLifetime());
    }

    @Override
    public String generateRefreshToken(TokenRequest request) {
        return generate(
                request,
                "refresh_token",
                tokenProperties.getRefreshTokenLifetime());
    }

    private String generate(
            TokenRequest request,
            String tokenType,
            java.time.Duration lifetime) {

        if (request == null || request.getClientId() == null
                || request.getClientId().isBlank()) {
            throw new TokenGenerationException(
                    "clientId must be provided", null);
        }

        JwtKeyVersion key = keyPolicyService.getActiveVersion();

        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(tokenProperties.getIssuer())
                .audience(tokenProperties.getAudience())
                .subject(
                        request.getSubject() == null
                                || request.getSubject().isBlank()
                                ? request.getClientId()
                                : request.getSubject())
                .issueTime(Date.from(now))
                .notBeforeTime(
                        Date.from(now.minus(
                                tokenProperties.getClockSkew())))
                .expirationTime(
                        Date.from(now.plus(lifetime)))
                .jwtID(jti)
                .claim("client_id", request.getClientId())
                .claim("token_type", tokenType)
                .claim("client_attrs", clientAttributes(
                        request, jti, tokenType))
                .build();

        JWSHeader header = new JWSHeader.Builder(
                JWSAlgorithm.parse(jwtProperties.getAlgorithm()))
                .type(JOSEObjectType.JWT)
                .keyID(key.keyId())
                .build();

        try {
            String encodedHeader = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            objectMapper.writeValueAsBytes(
                                    header.toJSONObject()));

            String encodedPayload = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            objectMapper.writeValueAsBytes(
                                    claims.toJSONObject()));

            String signingInput =
                    encodedHeader + "." + encodedPayload;

            String vaultSignature = keyStore.sign(
                    keyPolicyService.getPolicy().keyName(),
                    signingInput,
                    Math.toIntExact(key.version()));

            byte[] signature = decodeVaultSignature(vaultSignature);

            String token = signingInput + "."
                    + Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signature);

            verifyFreshlySignedToken(
                    token,
                    keyPolicyService.getPolicy().keyName(),
                    Math.toIntExact(key.version()));

            return token;

        } catch (JsonProcessingException ex) {
            throw new TokenGenerationException(
                    "Unable to serialize JWT", ex);
        } catch (ArithmeticException ex) {
            throw new TokenGenerationException(
                    "Vault key version is outside supported range", ex);
        } catch (RuntimeException ex) {
            if (ex instanceof TokenGenerationException) {
                throw ex;
            }
            throw new TokenGenerationException(
                    "Unable to sign JWT with Vault Transit", ex);
        }
    }

    private Map<String, Object> clientAttributes(
            TokenRequest request,
            String jti,
            String tokenType) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jti", jti);
        result.put("cid", request.getClientId());
        result.put("sub",
                request.getSubject() == null
                        ? request.getClientId()
                        : request.getSubject());
        result.put("jtt", tokenType);
        return result;
    }

    /**
     * Immediately re-verifies a just-signed token with the same
     * RSASSAVerifier every consuming service uses, using the algorithm
     * declared in the JWT header (RS256 = RSASSA-PKCS1-v1_5).
     *
     * Vault Transit's sign() call above explicitly requests
     * "signature_algorithm": "pkcs1v15" so this should always pass.
     * If it ever fails, Vault produced a signature using a different
     * padding scheme (most likely PSS) than the header claims — catching
     * that here, at issuance, is far cheaper than debugging it via a wave
     * of "Invalid JWT signature" errors across every downstream service.
     */
    private void verifyFreshlySignedToken(
            String token,
            String keyName,
            int keyVersion) {

        try {
            String pem = keyStore.getPublicKey(keyName, keyVersion);
            RSAPublicKey rsaPublicKey = parseRsaPublicKey(pem);

            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(new RSASSAVerifier(rsaPublicKey))) {
                throw new TokenGenerationException(
                        "Vault Transit signed this token but it does not "
                                + "verify as " + jwtProperties.getAlgorithm()
                                + " (RSASSA-PKCS1-v1_5). Vault is most likely "
                                + "signing with RSASSA-PSS instead, even though "
                                + "'signature_algorithm=pkcs1v15' was requested. "
                                + "Check the Vault Transit key configuration for '"
                                + keyName + "', or switch the platform to PS256 "
                                + "if PSS cannot be avoided.", null);
            }
        } catch (TokenGenerationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TokenGenerationException(
                    "Unable to self-verify freshly signed JWT", ex);
        }
    }

    private RSAPublicKey parseRsaPublicKey(String pem) throws Exception {
        String cleaned = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(cleaned);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private byte[] decodeVaultSignature(String value) {
        if (value == null || value.isBlank()) {
            throw new TokenGenerationException(
                    "Vault returned an empty signature", null);
        }

        int separator = value.lastIndexOf(':');
        String encoded =
                separator >= 0
                        ? value.substring(separator + 1)
                        : value;

        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException ex) {
            throw new TokenGenerationException(
                    "Vault returned an invalid signature", ex);
        }
    }
}
