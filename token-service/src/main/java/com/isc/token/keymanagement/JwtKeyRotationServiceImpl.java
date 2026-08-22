package com.isc.token.keymanagement;

public class JwtKeyRotationServiceImpl implements JwtKeyRotationService {
    private final JwtKeyPolicyService policyService;

    public JwtKeyRotationServiceImpl(JwtKeyPolicyService p) {
        policyService = p;
    }

    public JwtKeyVersion rotate() {
        return policyService.getActiveVersion();
    }
}
