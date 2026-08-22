package com.isc.common.security.jwt;

import com.isc.common.security.config.SecurityProperties;
import com.isc.common.security.exception.JwtValidationException;
import com.isc.common.security.keymanagement.KeyResolver;
import com.isc.common.security.model.JwtValidationResult;
import com.isc.common.security.model.SecurityPrincipal;
import com.isc.common.dto.ClientAttributes;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

public class JwtValidatorServiceImpl implements JwtValidatorService {
    private final KeyResolver resolver;
    private final SecurityProperties properties;
    private final Clock clock;

    public JwtValidatorServiceImpl(KeyResolver resolver, SecurityProperties properties) {
        this(resolver, properties, Clock.systemUTC());
    }

    JwtValidatorServiceImpl(KeyResolver resolver, SecurityProperties properties, Clock clock) {
        this.resolver = resolver;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public JwtValidationResult validate(String token) {
        try {
            if (token == null || token.isBlank()) throw new JwtValidationException("JWT must not be blank");
            SignedJWT jwt = SignedJWT.parse(token);
            if (!properties.getJwt().getAlgorithm().equals(jwt.getHeader().getAlgorithm().getName())) {
                throw new JwtValidationException("Unsupported JWT algorithm: " + jwt.getHeader().getAlgorithm());
            }
            String kid = jwt.getHeader().getKeyID();
            if (kid == null || kid.isBlank()) throw new JwtValidationException("JWT kid is missing");

            PublicKey key = resolver.resolve(kid);
            if (!(key instanceof RSAPublicKey rsaKey)) throw new JwtValidationException("JWT public key is not RSA");
            if (!jwt.verify(new RSASSAVerifier(rsaKey))) throw new JwtValidationException("Invalid JWT signature");

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Instant now = clock.instant();
            long skew = properties.getJwt().getClockSkew().toSeconds();

            if (claims.getExpirationTime() == null) throw new JwtValidationException("JWT exp claim is missing");
            if (claims.getExpirationTime().toInstant().isBefore(now.minusSeconds(skew)))
                throw new JwtValidationException("JWT is expired");
            if (claims.getNotBeforeTime() != null && claims.getNotBeforeTime().toInstant().isAfter(now.plusSeconds(skew)))
                throw new JwtValidationException("JWT is not yet valid");
            if (claims.getIssueTime() != null && claims.getIssueTime().toInstant().isAfter(now.plusSeconds(skew)))
                throw new JwtValidationException("JWT iat is in the future");

            String configuredIssuer = properties.getJwt().getIssuer();
            if (configuredIssuer != null && !configuredIssuer.isBlank()
                    && !Objects.equals(configuredIssuer, claims.getIssuer()))
                throw new JwtValidationException("Invalid JWT issuer");

            List<String> audiences = properties.getJwt().getAudiences();
            if (audiences != null && !audiences.isEmpty()) {
                List<String> tokenAud = claims.getAudience();
                if (tokenAud == null || tokenAud.stream().noneMatch(audiences::contains))
                    throw new JwtValidationException("Invalid JWT audience");
            }

            return new JwtValidationResult(true, claims.getSubject(), claims.getIssuer(), kid, claims.getClaims());
        } catch (JwtValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JwtValidationException("JWT validation failed", ex);
        }
    }

    @Override
    public SecurityPrincipal validateAndExtractPrincipal(String token) {
        JwtValidationResult result = validate(token);
        Map<String, Object> claims = result.claims();
        Set<String> authorities = extractAuthorities(claims);
        ClientAttributes clientAttributes = extractClientAttributes(claims);
        return new SecurityPrincipal(
                result.subject(),
                authorities,
                claims,
                clientAttributes);
    }

    @Override
    public boolean isValid(String token) {
        try {
            validate(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private Set<String> extractAuthorities(Map<String, Object> claims) {
        Set<String> result = new LinkedHashSet<>();
        addClaimValues(result, claims.get("authorities"));
        addClaimValues(result, claims.get("roles"));
        addClaimValues(result, claims.get("scope"));
        addClaimValues(result, claims.get("scp"));
        return Collections.unmodifiableSet(result);
    }

    private void addClaimValues(Set<String> target, Object value) {
        if (value instanceof Collection<?> c) c.forEach(v -> {
            if (v != null) target.add(v.toString());
        });
        else if (value != null) {
            for (String item : value.toString().split("\s+")) if (!item.isBlank()) target.add(item);
        }
    }

    private ClientAttributes extractClientAttributes(Map<String, Object> claims) {
        Object value = claims.get("client_attrs");
        if (value instanceof Map<?, ?> map) {
            ClientAttributes ca = new ClientAttributes();
            ca.setJti(string(map, "jti"));
            ca.setSid(string(map, "sid"));
            ca.setDid(string(map, "did"));
            ca.setCid(string(map, "cid"));
            ca.setSub(string(map, "sub"));
            ca.setJtt(string(map, "jtt"));
            return ca;
        }
        return null;
    }

    private String string(Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }
}
