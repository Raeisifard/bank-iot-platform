package com.isc.tokenservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Schema(description = "JWT Key Policy configuration")
public class JwtKeyPolicy {

    @Schema(description = "Currently active key information")
    private KeyInfo active;

    @Schema(description = "Previous active key information")
    private KeyInfo inactive;

    @Schema(description = "Next key to be used for rotation")
    private KeyInfo next;

    @Schema(description = "List of retired/expired keys")
    private List<KeyInfo> retired;

    @Data
    @Schema(description = "Key information details")
    public static class KeyInfo {
        @Schema(description = "Key identifier", example = "key-123")
        private String kid;

        @Schema(description = "Vault key version number", example = "5")
        private Integer vkv; //vaultKeyVersion

        @Schema(description = "Key validity start timestamp", example = "2026-01-01T00:00:00Z")
        private Instant vlf; //validFrom

        @Schema(description = "Key validity end timestamp", example = "2026-02-31T23:59:59Z")
        private Instant vlt; //validTo
    }
}