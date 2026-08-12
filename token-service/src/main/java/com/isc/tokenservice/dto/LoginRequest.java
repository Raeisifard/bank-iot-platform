package com.isc.tokenservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    /*@Schema(example = "behnam")
    private String username;
    @Schema(example = "MyP@ssword")
    private String password;*/
    @Schema(example = "CUST-1001")
    private String userId;
    @Schema(example = "DEVICE-1")
    private String deviceId;
    @Schema(example = "ANDROID")
    private String clientId;
    @Schema(example = "123456")
    private String otp;
}