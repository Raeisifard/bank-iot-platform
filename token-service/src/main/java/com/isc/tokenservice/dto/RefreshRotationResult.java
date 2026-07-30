package com.isc.tokenservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshRotationResult {

    private String refreshToken;

    private String customerId;

    private String deviceId;

    private String clientId;

    private String sessionId;
}
