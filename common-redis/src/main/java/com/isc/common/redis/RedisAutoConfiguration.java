package com.isc.common.redis;

import io.lettuce.core.ClientOptions;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.URI;
import java.time.Duration;

@AutoConfiguration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    RedisPropertiesValidated redisPropertiesValidated(
            RedisProperties properties) {

        if (properties.getEnabled() == null) {
            throw new IllegalStateException(
                    "Mandatory property 'common.redis.enabled' is missing. " +
                            "Set common.redis.enabled=true or false."
            );
        }

        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            throw new IllegalStateException(
                    "Property 'common.redis.url' must not be blank."
            );
        }

        if (properties.getDatabase() < 0) {
            throw new IllegalStateException(
                    "Property 'common.redis.database' must be >= 0."
            );
        }

        if (properties.getTimeout() == null
                || properties.getTimeout().isZero()
                || properties.getTimeout().isNegative()) {

            throw new IllegalStateException(
                    "Property 'common.redis.timeout' must be greater than zero."
            );
        }

        return new RedisPropertiesValidated();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "common.redis",
            name = "enabled",
            havingValue = "true"
    )
    @ConditionalOnMissingBean
    LettuceConnectionFactory redisConnectionFactory(
            RedisProperties properties) {

        URI uri = URI.create(properties.getUrl());

        String scheme = uri.getScheme();

        if (!"redis".equalsIgnoreCase(scheme)
                && !"rediss".equalsIgnoreCase(scheme)) {

            throw new IllegalStateException(
                    "Unsupported Redis URL scheme: " + scheme +
                            ". Supported schemes are redis:// and rediss://."
            );
        }

        boolean ssl = "rediss".equalsIgnoreCase(scheme);

        String host = uri.getHost();

        if (host == null || host.isBlank()) {
            throw new IllegalStateException(
                    "Redis URL must contain a host: "
                            + properties.getUrl()
            );
        }

        int port = uri.getPort();

        if (port < 0) {
            port = ssl ? 6380 : 6379;
        }

        RedisStandaloneConfiguration server =
                new RedisStandaloneConfiguration(host, port);

        configureDatabase(server, uri, properties);

        configureCredentials(server, uri, properties);

        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder()
                        .commandTimeout(properties.getTimeout())
                        .clientOptions(
                                ClientOptions.builder()
                                        .autoReconnect(true)
                                        .build()
                        );

        if (ssl) {
            builder.useSsl();
        }

        LettuceClientConfiguration clientConfiguration =
                builder.build();

        return new LettuceConnectionFactory(
                server,
                clientConfiguration
        );
    }

    private void configureDatabase(
            RedisStandaloneConfiguration server,
            URI uri,
            RedisProperties properties) {

        int database = properties.getDatabase();

        String path = uri.getPath();

        if (path != null && path.length() > 1) {

            String databasePart = path.substring(1);

            try {
                database = Integer.parseInt(databasePart);
            } catch (NumberFormatException ex) {
                throw new IllegalStateException(
                        "Invalid Redis database in URL: "
                                + properties.getUrl(),
                        ex
                );
            }

            if (database < 0) {
                throw new IllegalStateException(
                        "Redis database must be >= 0: " + database
                );
            }
        }

        server.setDatabase(database);
    }

    private void configureCredentials(
            RedisStandaloneConfiguration server,
            URI uri,
            RedisProperties properties) {

        String username = null;
        String password = properties.getPassword();

        String userInfo = uri.getUserInfo();

        if (userInfo != null && !userInfo.isBlank()) {

            int separator = userInfo.indexOf(':');

            if (separator >= 0) {

                username = userInfo.substring(0, separator);

                String urlPassword =
                        userInfo.substring(separator + 1);

                if (password == null || password.isBlank()) {
                    password = urlPassword;
                }

            } else {
                username = userInfo;
            }
        }

        if (username != null && !username.isBlank()) {
            server.setUsername(username);
        }

        if (password != null && !password.isBlank()) {
            server.setPassword(RedisPassword.of(password));
        }
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "common.redis",
            name = "enabled",
            havingValue = "true"
    )
    @ConditionalOnMissingBean
    StringRedisTemplate stringRedisTemplate(
            LettuceConnectionFactory connectionFactory) {

        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "common.redis",
            name = "enabled",
            havingValue = "true"
    )
    @ConditionalOnMissingBean(RedisOperations.class)
    RedisOperations redisOperations(
            StringRedisTemplate redis) {

        return new RedisOperationsImpl(redis);
    }

    static final class RedisPropertiesValidated {
    }
}