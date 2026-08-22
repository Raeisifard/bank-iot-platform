package com.isc.token.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        TokenServiceProperties.class,
        JwtProperties.class
})
public class TokenServiceConfiguration {
}
