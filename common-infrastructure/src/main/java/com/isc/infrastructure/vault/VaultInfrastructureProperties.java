package com.isc.infrastructure.vault;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "infrastructure.vault")
public class VaultInfrastructureProperties {
    private boolean enabled = false;
    private String uri;
    private String token;
    private String namespace;
}
