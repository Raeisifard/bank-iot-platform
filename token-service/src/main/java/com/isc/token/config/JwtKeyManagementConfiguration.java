package com.isc.token.config;

import com.isc.common.vault.jwt.VaultJwtKeyStore;
import com.isc.token.jwks.JwkProvider;
import com.isc.token.jwks.JwkProviderImpl;
import com.isc.token.jwks.JwksService;
import com.isc.token.jwks.JwksServiceImpl;
import com.isc.token.keymanagement.JwtKeyPolicy;
import com.isc.token.keymanagement.JwtKeyPolicyService;
import com.isc.token.keymanagement.JwtKeyRotationService;
import com.isc.token.keymanagement.JwtKeyRotationServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtKeyPolicyProperties.class)
public class JwtKeyManagementConfiguration {

    @Bean
    public JwtKeyPolicy jwtKeyPolicy(
            JwtKeyPolicyProperties properties) {
        return new JwtKeyPolicy(
                properties.getKeyName(),
                properties.getAlgorithm(),
                properties.getRotationInterval(),
                properties.getPublicationOverlap(),
                properties.getMaxPublishedVersions());
    }

    @Bean
    public JwkProvider jwkProvider(
            VaultJwtKeyStore keyStore,
            JwtKeyPolicyProperties properties) {
        return new JwkProviderImpl(keyStore, properties);
    }

    @Bean
    public JwksService jwksService(
            JwtKeyPolicyService policyService,
            JwkProvider jwkProvider) {
        return new JwksServiceImpl(policyService, jwkProvider);
    }

    @Bean
    public JwtKeyRotationService jwtKeyRotationService(
            JwtKeyPolicyService policyService) {
        return new JwtKeyRotationServiceImpl(policyService);
    }
}
