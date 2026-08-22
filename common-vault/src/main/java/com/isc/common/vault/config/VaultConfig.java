package com.isc.common.vault.config;

import java.net.URI;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

@Configuration
@EnableConfigurationProperties(VaultProperties.class)
@ConditionalOnProperty(prefix = "common.vault", name = "enabled", havingValue = "true")
public class VaultConfig {

    @Bean
    public VaultEndpoint vaultEndpoint(VaultProperties properties) {
        URI uri = URI.create(properties.getUri());

        VaultEndpoint endpoint = VaultEndpoint.create(
                uri.getHost(),
                uri.getPort()
        );

        if (uri.getScheme() != null) {
            endpoint.setScheme(uri.getScheme());
        }

        return endpoint;
    }

    @Bean
    public VaultTemplate vaultTemplate(
            VaultEndpoint endpoint,
            VaultProperties properties) {

        if (properties.getToken() == null || properties.getToken().isBlank()) {
            throw new IllegalStateException(
                    "vault.token must be configured when vault.enabled=true");
        }

        return new VaultTemplate(
                endpoint,
                new TokenAuthentication(properties.getToken())
        );
    }
}
