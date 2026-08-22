package com.isc.common.security.jwt;

import com.isc.common.security.config.SecurityProperties;
import com.isc.common.security.keymanagement.KeyResolver;
import org.junit.jupiter.api.Test;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import static org.junit.jupiter.api.Assertions.*;

class JwtValidatorServiceTest {
 @Test void shouldRejectMalformedToken() {
  var props=new SecurityProperties();
  KeyResolver resolver=kid -> null;
  var service=new JwtValidatorServiceImpl(resolver,props);
  assertThrows(RuntimeException.class,()->service.validate("bad"));
 }
 @Test void shouldValidateSignatureAndClaims() throws Exception {
  var kp=KeyPairGenerator.getInstance("RSA").generateKeyPair();
  var props=new SecurityProperties();
  var jwtProps=props.getJwt(); jwtProps.setIssuer("bank"); jwtProps.getAudiences().add("api");
  var resolver=(KeyResolver) kid -> kp.getPublic();
  var service=new JwtValidatorServiceImpl(resolver,props,Clock.fixed(Instant.parse("2026-08-17T10:00:00Z"),ZoneOffset.UTC));
  var claims=new JWTClaimsSet.Builder().subject("customer-1").issuer("bank").audience("api")
    .issueTime(Date.from(Instant.parse("2026-08-17T09:59:00Z")))
    .expirationTime(Date.from(Instant.parse("2026-08-17T11:00:00Z"))).claim("scope","ack.read").build();
  SignedJWT jwt=new SignedJWT(new com.nimbusds.jose.JWSHeader.Builder(JWSAlgorithm.RS256).keyID("kid-1").build(),claims);
  JWSSigner signer=new RSASSASigner(kp.getPrivate()); jwt.sign(signer);
  var result=service.validate(jwt.serialize());
  assertTrue(result.valid()); assertEquals("customer-1",result.subject());
 }
}
