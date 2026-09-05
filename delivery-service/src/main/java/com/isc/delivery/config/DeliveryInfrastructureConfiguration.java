package com.isc.delivery.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DeliveryProperties.class)
@ConditionalOnProperty(prefix = "delivery", name = {"enabled", "oracle.enabled"}, havingValue = "true")
public class DeliveryInfrastructureConfiguration {

    @Bean
    DataSource deliveryDataSource(DeliveryProperties properties) {
        DeliveryProperties.Oracle oracle = properties.getOracle();
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(required(oracle.getUrl(), "delivery.oracle.url"))
                .username(required(oracle.getUsername(), "delivery.oracle.username"))
                .password(required(oracle.getPassword(), "delivery.oracle.password"))
                .build();
    }

    @Bean
    JdbcTemplate deliveryJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private String required(String value, String property) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Property '" + property + "' must not be blank");
        }
        return value;
    }
}
