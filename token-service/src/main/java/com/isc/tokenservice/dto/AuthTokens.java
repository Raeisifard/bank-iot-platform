package com.isc.tokenservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthTokens {

    private String accessToken;

    private String refreshToken;

    private String sessionId;
}
