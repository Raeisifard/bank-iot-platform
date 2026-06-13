package com.isc.tokenservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.tokenservice.config.JwtProperties;
import com.isc.tokenservice.dto.AuthTokens;
import com.isc.tokenservice.dto.ClientAttributes;
import com.isc.tokenservice.dto.JwtKeyPolicy;
import com.isc.tokenservice.identity.SessionService;
import com.isc.tokenservice.identity.VaultTransitJwtSigner;
import com.isc.tokenservice.vault.JwtKeyPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtGeneratorService {

    private final SessionService sessionService;

    private final JwtProperties jwtProperties;

    private final JwtKeyPolicyService policyService;

    private final VaultTransitJwtSigner signer;

    private final ObjectMapper objectMapper;

    public AuthTokens generate(
            String customerId,
            String deviceId,
            String clientId) throws Exception {

        //--------------------------------
        // active key from cached policy
        //--------------------------------

        JwtKeyPolicy policy =
                policyService.getCachedPolicy();

        JwtKeyPolicy.KeyInfo active =
                policy.getActive();

        String kid =
                active.getKid();

        //--------------------------------
        // session
        //--------------------------------

        String sessionId =
                "SID-" + UUID.randomUUID();

        String jwtId =
                UUID.randomUUID().toString();

        sessionService.createSession(
                customerId,
                deviceId,
                clientId,
                jwtId
        );

        //--------------------------------
        // timestamps
        //--------------------------------

        Instant now =
                Instant.now();

        //--------------------------------
        // JWT Header
        //--------------------------------

        Map<String, Object> header =
                new LinkedHashMap<>();

        header.put("alg", "RS256");
        header.put("typ", "JWT");
        header.put("kid", kid);

        //--------------------------------
        // JWT Payload
        //--------------------------------

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put("sub", customerId);
        //payload.put("iss", jwtProperties.getIssuer());
        //payload.put("aud", List.of(jwtProperties.getAudience()));
        //payload.put("jti", "JTI-" + jwtId);
        //payload.put("sid", sessionId);
        //payload.put("did", deviceId);
        //payload.put("cid", clientId);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(Duration.parse(jwtProperties.getAccessTokenTtl()).toSeconds()).getEpochSecond());
        //This section is for EMQX's "client_attrs" preparing
        ClientAttributes attrs = new ClientAttributes(
                jwtProperties.getIssuer(),
                jwtProperties.getAudience(),
                "JTI-" + jwtId,
                sessionId,
                deviceId,
                clientId);
        payload.put("client_attrs", attrs);

        //--------------------------------
        // Base64Url
        //--------------------------------

        String encodedHeader =
                base64Url(
                        objectMapper.writeValueAsBytes(header)
                );

        String encodedPayload =
                base64Url(
                        objectMapper.writeValueAsBytes(payload)
                );

        //--------------------------------
        // signing input
        //--------------------------------

        String signingInput =
                encodedHeader + "." + encodedPayload;

        //--------------------------------
        // sign using Vault Transit
        //--------------------------------

        String jwtSignature =
                signer.sign(
                        signingInput,
                        active.getVkv()
                );

        //--------------------------------
        // final JWT
        //--------------------------------

        String accessToken =
                signingInput + "." + jwtSignature;

        //--------------------------------
        // refresh token
        //--------------------------------

        String refreshToken =
                UUID.randomUUID().toString();

        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .sessionId(sessionId)
                .build();
    }

    //--------------------------------
    // base64url
    //--------------------------------

    private String base64Url(byte[] bytes) {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}