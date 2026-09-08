package com.isc.clientidentity.api;

import jakarta.validation.constraints.NotBlank;

public record RegisterClientRequest(
        @NotBlank String cid,
        @NotBlank String did,
        @NotBlank String publicKey,
        @NotBlank String keyAlgorithm,
        String attestation) {
}