package com.isc.token.jwks;

import com.isc.token.keymanagement.JwtKeyPolicyService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JwksServiceImpl implements JwksService {

    private final JwtKeyPolicyService policy;
    private final JwkProvider provider;

    @Override
    public List<Map<String, Object>> getJwks() {
        return policy.getPublishableVersions()
                .stream()
                .map(provider::toJwk)
                .toList();
    }
}
