package com.isc.infrastructure.vault;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(VaultTemplate.class)
@ConditionalOnProperty(prefix = "infrastructure.vault", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(VaultInfrastructureProperties.class)
public class VaultInfrastructureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(VaultTemplate.class)
    public VaultTemplate vaultTemplate(VaultInfrastructureProperties p) {
        if (p.getUri() == null || p.getUri().isBlank()) {
            throw new IllegalStateException("infrastructure.vault.uri must be configured");
        }
        if (p.getToken() == null || p.getToken().isBlank()) {
            throw new IllegalStateException("infrastructure.vault.token must be configured");
        }

        URI uri = URI.create(p.getUri());

        VaultEndpoint endpoint = VaultEndpoint.create(uri.getHost(), uri.getPort());
        endpoint.setScheme(uri.getScheme());

        if (p.getNamespace() != null && !p.getNamespace().isBlank()) {
            endpoint.setPath(p.getNamespace());
        }

        return new VaultTemplate(endpoint, new TokenAuthentication(p.getToken()));
    }
}
