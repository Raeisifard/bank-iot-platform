package com.isc.security.service;

import com.isc.security.exception.InvalidTokenException;
import com.isc.security.exception.TokenExpiredException;
import com.isc.security.model.JwtClaims;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtValidatorServiceImpl implements JwtValidatorService {

    private final PublicKeyProvider keyProvider;

    @Override
    public JwtClaims validate(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            String kid = jwt.getHeader().getKeyID();

            PublicKey publicKey = keyProvider.getPublicKey(kid);

            RSASSAVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);

            if (!jwt.verify(verifier)) {
                throw new InvalidTokenException("Jwt verification failed");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            Date expiration = claims.getExpirationTime();

            if (expiration != null && expiration.before(new Date())) {
                throw new TokenExpiredException();
            }

            return JwtClaims.builder()
                    .sessionId(claims.getStringClaim("sid"))
                    .clientId(claims.getStringClaim("clientId"))
                    .userId(claims.getSubject())
                    .build();
        } catch (ParseException | JOSEException e) {
            throw new InvalidTokenException(e);
        }
    }

    @Override
    public JwtClaims validate(String jwt, boolean verifyExpiration) {
        if (verifyExpiration)
            return validate(jwt);
        else
            return null;
    }

    @Override
    public boolean isValid(String jwt) {
        return validate(jwt) != null;
    }
}