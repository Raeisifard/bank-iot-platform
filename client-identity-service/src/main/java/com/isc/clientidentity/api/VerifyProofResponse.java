package com.isc.clientidentity.api;

public record VerifyProofResponse(
        boolean valid,
        String cid,
        String did,
        String clientStatus,
        String identityLevel) {
}