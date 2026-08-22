package com.isc.token.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "token.service")
public class TokenServiceProperties {
    private boolean enabled = true;
    private String issuer = "isc-token-service";
    private String audience = "isc-services";
    private Duration clockSkew = Duration.ofSeconds(30);
    private Duration accessTokenLifetime = Duration.ofMinutes(15);
    private Duration refreshTokenLifetime = Duration.ofDays(30);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean v) {
        enabled = v;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String v) {
        issuer = v;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String v) {
        audience = v;
    }

    public Duration getClockSkew() {
        return clockSkew;
    }

    public void setClockSkew(Duration v) {
        clockSkew = v;
    }

    public Duration getAccessTokenLifetime() {
        return accessTokenLifetime;
    }

    public void setAccessTokenLifetime(Duration v) {
        accessTokenLifetime = v;
    }

    public Duration getRefreshTokenLifetime() {
        return refreshTokenLifetime;
    }

    public void setRefreshTokenLifetime(Duration v) {
        refreshTokenLifetime = v;
    }
}
