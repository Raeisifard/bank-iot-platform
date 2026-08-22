package com.isc.token.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "token.jwt")
public class JwtProperties {
    private String algorithm = "RS256";
    private String keyIdPrefix = "jwt";
    private Duration accessTokenLifetime = Duration.ofMinutes(15);
    private Duration refreshTokenLifetime = Duration.ofDays(30);

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String v) {
        algorithm = v;
    }

    public String getKeyIdPrefix() {
        return keyIdPrefix;
    }

    public void setKeyIdPrefix(String v) {
        keyIdPrefix = v;
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
