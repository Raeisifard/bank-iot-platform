package com.isc.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.security.exception.InvalidTokenException;
import com.isc.security.exception.TokenExpiredException;
import com.isc.security.model.JwtClaims;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtValidatorServiceImpl implements JwtValidatorService {

    private final PublicKeyProvider keyProvider;
    private final ObjectMapper objectMapper;

    @Override
    public JwtClaims validate(String token) {
        return validate(token, true);
    }

    @Override
    public JwtClaims validate(String token, boolean verifyExpiration) {

        try {
            SignedJWT jwt = SignedJWT.parse(token);

            String kid = jwt.getHeader().getKeyID();

            if (kid == null || kid.isBlank()) {
                throw new InvalidTokenException("JWT kid is missing");
            }

            RSAPublicKey publicKey =
                    (RSAPublicKey) keyProvider.getPublicKey(kid);

            RSASSAVerifier verifier = new RSASSAVerifier(publicKey);

            if (!jwt.verify(verifier)) {
                throw new InvalidTokenException("JWT verification failed");
            }

            /*
             * Deserialize the complete JWT payload into our own model.
             *
             * This avoids using Nimbus JWTClaimsSet as the application's
             * claims representation.
             */
            JwtClaims claims = objectMapper.readValue(
                    jwt.getPayload().toString(),
                    JwtClaims.class
            );

            if (verifyExpiration) {
                validateExpiration(claims);
            }

            return claims;

        } catch (ParseException | JOSEException e) {
            throw new InvalidTokenException(e);
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid JWT claims", e);
        }
    }

    private void validateExpiration(JwtClaims claims) {

        Instant expiration = claims.getExp();

        if (expiration == null) {
            throw new InvalidTokenException("JWT expiration claim is missing");
        }

        if (expiration.isBefore(Instant.now())) {
            throw new TokenExpiredException();
        }
    }

    @Override
    public boolean isValid(String jwt) {
        try {
            validate(jwt);
            return true;
        } catch (InvalidTokenException | TokenExpiredException e) {
            return false;
        }
    }
}
