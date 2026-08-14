package com.isc.infrastructure.oracle;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "infrastructure.oracle")
public class OracleInfrastructureProperties {
    private boolean enabled = false;
    private String url;
    private String username;
    private String password;
    private String driverClassName = "oracle.jdbc.OracleDriver";
    private int maximumPoolSize = 20;
    private int minimumIdle = 5;
    private long connectionTimeoutMs = 30000;
    private long idleTimeoutMs = 600000;
    private long maxLifetimeMs = 1800000;
    private long validationTimeoutMs = 5000;
}
