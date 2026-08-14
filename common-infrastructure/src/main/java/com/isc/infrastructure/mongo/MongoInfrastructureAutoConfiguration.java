package com.isc.infrastructure.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.TimeUnit;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MongoClient.class)
@ConditionalOnProperty(prefix = "infrastructure.mongo", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MongoInfrastructureProperties.class)
public class MongoInfrastructureAutoConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(MongoClient.class)
    public MongoClient mongoClient(MongoInfrastructureProperties p) {
        if (p.getUri() == null || p.getUri().isBlank()) {
            throw new IllegalStateException("infrastructure.mongo.uri must be configured");
        }

        ConnectionString connectionString = new ConnectionString(p.getUri());

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings(s -> s
                        .connectTimeout(p.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                        .readTimeout(p.getConnectTimeoutSeconds(), TimeUnit.SECONDS))
                .applyToClusterSettings(s ->
                        s.serverSelectionTimeout(
                                p.getServerSelectionTimeoutSeconds(), TimeUnit.SECONDS))
                .applyToConnectionPoolSettings(s -> s
                        .maxSize(p.getMaxConnectionPoolSize())
                        .minSize(p.getMinConnectionPoolSize()))
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    @ConditionalOnMissingBean(MongoTemplate.class)
    public MongoTemplate mongoTemplate(
            MongoClient mongoClient,
            MongoInfrastructureProperties p) {
        if (p.getDatabase() == null || p.getDatabase().isBlank()) {
            throw new IllegalStateException("infrastructure.mongo.database must be configured");
        }
        return new MongoTemplate(mongoClient, p.getDatabase());
    }
}
