package com.isc.tokenservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.common.enums.JwtTokenType;
import com.isc.tokenservice.config.JwtProperties;
import com.isc.tokenservice.dto.AuthTokens;
import com.isc.tokenservice.dto.ClientAttributes;
import com.isc.tokenservice.dto.JwtKeyPolicy;
import com.isc.tokenservice.vault.VaultTransitJwtSigner;
import com.isc.tokenservice.vault.JwtKeyPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtGeneratorService {

    private final JwtKeyPolicyService policyService;
    private final VaultTransitJwtSigner signer;
    private final ObjectMapper objectMapper;
    private final JwtProperties properties;
    private final JwtProperties jwtProperties;

    public AuthTokens issue(
            String customerId,
            String deviceId,
            String clientId,
            String sessionId,
            //String refreshId
            JwtTokenType jtt
    ) throws Exception {

        JwtKeyPolicy policy = policyService.getCachedPolicy();
        String kid = policy.getActive().getKid();

        Instant now = Instant.now();

        // ---------------- HEADER ----------------
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "RS256");
        header.put("typ", "JWT");
        header.put("kid", kid);

        // ---------------- ACCESS JTI ----------------
        String jti = jtt.buildJti(UUID.randomUUID().toString());

        // ---------------- PAYLOAD ----------------
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", customerId);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp",
                now.plusSeconds(properties.getAccessTokenTtl().toSeconds()).getEpochSecond()
        );

        //payload.put("jti", accessJti);
        payload.put("sid", sessionId);

        // مهم برای trace کامل
        //payload.put("rti", refreshId);

        ClientAttributes attrs = new ClientAttributes(
                properties.getIssuer(),
                properties.getAudience(),
                jti,
                sessionId,
                deviceId,
                clientId
        );

        payload.put("client_attrs", attrs);

        // ---------------- ENCODE ----------------
        String encodedHeader =
                base64Url(objectMapper.writeValueAsBytes(header));

        String encodedPayload =
                base64Url(objectMapper.writeValueAsBytes(payload));

        String signingInput =
                encodedHeader + "." + encodedPayload;

        // ---------------- VAULT SIGN ----------------
        String signature =
                signer.sign(signingInput, policy.getActive().getVkv());

        String accessToken =
                signingInput + "." + signature;

        // ---------------- REFRESH (opaque or jwt) ----------------
        //String refreshToken = generateRefreshToken(refreshId, sessionId);

        return AuthTokens.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(Instant.now().plus(jwtProperties.getAccessTokenTtl()))
                .build();
    }

    /*private String generateRefreshToken(String refreshId, String sid) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        (refreshId + ":" + sid + ":" + UUID.randomUUID())
                                .getBytes()
                );
    }*/

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}