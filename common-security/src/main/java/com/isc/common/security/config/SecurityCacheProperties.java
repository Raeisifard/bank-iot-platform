package com.isc.common.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties("common.security.cache.public-keys")
public class SecurityCacheProperties {
    private boolean enabled = true;
    private Duration ttl = Duration.ofMinutes(15);
    private long maximumSize = 100;
}
