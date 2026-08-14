package com.isc.infrastructure.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "infrastructure.redis")
public class RedisInfrastructureProperties {
    private boolean enabled = false;
    private String host = "localhost";
    private int port = 6379;
    private String password;
    private int database = 0;
    private int timeoutSeconds = 3;
    private int shutdownTimeoutSeconds = 5;
}
