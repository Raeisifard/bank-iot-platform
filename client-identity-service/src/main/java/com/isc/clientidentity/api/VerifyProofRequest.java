package com.isc.clientidentity.api;

import jakarta.validation.constraints.NotBlank;

public record VerifyProofRequest(
        @NotBlank String cid,
        @NotBlank String did,
        @NotBlank String challengeId,
        @NotBlank String signature) {
}