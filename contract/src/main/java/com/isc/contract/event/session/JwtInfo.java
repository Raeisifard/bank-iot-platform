package com.isc.contract.event.session;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "JwtInfo",
        description = "JWT claims associated with the MQTT session."
)
public class JwtInfo {

    @Schema(
            description = "Session identifier.",
            example = "sess-4f2d9f32"
    )
    private String sid;

    @Schema(
            description = "JWT unique identifier.",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String jti;

    @Schema(
            description = "JWT issuer.",
            example = "auth-service"
    )
    private String iss;

    @Schema(
            description = "Device identifier.",
            example = "device-987654"
    )
    private String did;

    @Schema(
            description = "Customer identifier.",
            example = "customer-10001"
    )
    private String cid;

    @Schema(
            description = "JWT audience.",
            example = "mqtt-broker"
    )
    private String aud;
}