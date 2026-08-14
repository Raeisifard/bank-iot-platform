package com.isc.infrastructure.oracle;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(prefix = "infrastructure.oracle", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(OracleInfrastructureProperties.class)
public class OracleInfrastructureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public HikariDataSource oracleDataSource(OracleInfrastructureProperties p) {
        if (p.getUrl() == null || p.getUrl().isBlank()) {
            throw new IllegalStateException("infrastructure.oracle.url must be configured");
        }

        HikariConfig c = new HikariConfig();
        c.setJdbcUrl(p.getUrl());
        c.setUsername(p.getUsername());
        c.setPassword(p.getPassword());
        c.setDriverClassName(p.getDriverClassName());
        c.setMaximumPoolSize(p.getMaximumPoolSize());
        c.setMinimumIdle(p.getMinimumIdle());
        c.setConnectionTimeout(p.getConnectionTimeoutMs());
        c.setIdleTimeout(p.getIdleTimeoutMs());
        c.setMaxLifetime(p.getMaxLifetimeMs());
        c.setValidationTimeout(p.getValidationTimeoutMs());
        c.setPoolName("oracle-infrastructure-pool");
        return new HikariDataSource(c);
    }

    @Bean
    @ConditionalOnMissingBean(JdbcTemplate.class)
    public JdbcTemplate oracleJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
