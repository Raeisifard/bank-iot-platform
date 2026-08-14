package com.isc.infrastructure.mongo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "infrastructure.mongo")
public class MongoInfrastructureProperties {
    private boolean enabled = false;
    private String uri;
    private String database;
    private int connectTimeoutSeconds = 10;
    private int serverSelectionTimeoutSeconds = 10;
    private int maxConnectionPoolSize = 100;
    private int minConnectionPoolSize = 10;
}
