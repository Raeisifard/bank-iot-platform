package com.isc.tokenservice.config;

import com.isc.tokenservice.dto.Rotation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@Getter
@Setter

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String issuer;
    private String audience;

    private Duration accessTokenTtl;
    private Duration refreshTokenTtl;
    private Duration refreshTokenAuditTtl;

    private String kvMount;
    private String kvPath;

    private String kidPrefix;
    private String kidFormat;

    private Duration sessionIdleTtl;
    private Duration sessionAbsoluteTtl;
    private Duration sessionAuditTtl;

    @NestedConfigurationProperty
    private Rotation rotation;
}