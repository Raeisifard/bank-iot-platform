package com.isc.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vault.jwt")
public class VaultJwtProperties {

    /**
     * jwt/keys
     */
    private String path;
}