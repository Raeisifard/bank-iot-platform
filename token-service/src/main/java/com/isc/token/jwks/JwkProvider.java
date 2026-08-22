package com.isc.token.jwks;

import com.isc.token.keymanagement.JwtKeyVersion;

import java.util.Map;

public interface JwkProvider {
    Map<String, Object> toJwk(JwtKeyVersion version);
}
