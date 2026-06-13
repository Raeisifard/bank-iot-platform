package com.isc.tokenservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @Schema(example = "CUST-1001")
    private String customerId;
    @Schema(example = "DEVICE-1")
    private String deviceId;
    @Schema(example = "ANDROID")
    private String clientId;
}