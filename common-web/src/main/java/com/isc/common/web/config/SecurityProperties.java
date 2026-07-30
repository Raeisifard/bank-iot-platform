package com.isc.common.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.authentication")
public class SecurityProperties {
    /**
     * Enable/Disable authentication
     */
    private boolean enabled = true;

}
