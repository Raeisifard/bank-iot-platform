package com.isc.kafka.session.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Redis redis = new Redis();

    @Data
    public static class Redis {

        private String sessionPrefix;

        private String clientSessionsPrefix;

        private String onlineUsersKey;

        private Integer defaultTtlHours;
    }
}
