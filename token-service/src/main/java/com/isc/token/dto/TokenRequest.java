package com.isc.token.dto;

import jakarta.validation.constraints.NotBlank;

public class TokenRequest {
    @NotBlank
    private String clientId;
    private String clientSecret;
    private String subject;
    private String tokenType = "access_token";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String v) {
        clientId = v;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String v) {
        clientSecret = v;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String v) {
        subject = v;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String v) {
        tokenType = v;
    }
}
