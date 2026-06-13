package com.isc.mqtt.ingress.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.isc.contract.event.session.JwtInfo;
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
        name = "EmqxClientConnectedRequest",
        description = "Payload received from EMQX client.connected webhook."
)
public class EmqxClientConnectedRequest {

    @Schema(
            description = "Timestamp received from EMQX.",
            example = "1781100207683"
    )
    private Long timestamp;

    @JsonProperty("client_id")
    @Schema(
            description = "MQTT Client Identifier.",
            example = "CUST-1002"
    )
    private String clientId;

    @Schema(
            description = "Authenticated username.",
            example = "behnam"
    )
    private String username;

    @JsonProperty("ip_address")
    @Schema(
            description = "Client IP address and source port.",
            example = "192.168.218.99:54071"
    )
    private String ipAddress;

    @Schema(
            description = "MQTT protocol version.",
            example = "5"
    )
    private Integer protocol;

    @Schema(
            description = "JWT information extracted during authentication."
    )
    private JwtInfo jwt;
}