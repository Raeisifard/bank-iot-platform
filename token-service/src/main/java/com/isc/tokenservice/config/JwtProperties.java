package com.isc.tokenservice.config;

import com.isc.tokenservice.dto.Rotation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String issuer;
    private String audience;

    private String accessTokenTtl;
    private String refreshTokenTtl;

    private String kvMount;
    private String kvPath;

    private String kidPrefix;
    private String kidFormat;

    @NestedConfigurationProperty
    private Rotation rotation;
}