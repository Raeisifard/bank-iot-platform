package com.isc.common.web.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CommonWebProperties.class)
public class CommonWebConfiguration {
}
