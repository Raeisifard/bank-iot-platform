package com.isc.common.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.isc.common.security.authentication.AuthenticationService;
import com.isc.common.security.authentication.AuthenticationServiceImpl;
import com.isc.common.security.authorization.AuthorizationService;
import com.isc.common.security.authorization.AuthorizationServiceImpl;
import com.isc.common.security.cache.SecurityKeyCache;
import com.isc.common.security.cache.SecurityKeyCacheImpl;
import com.isc.common.security.config.SecurityCacheProperties;
import com.isc.common.security.config.SecurityProperties;
import com.isc.common.security.jwt.JwtValidatorService;
import com.isc.common.security.jwt.JwtValidatorServiceImpl;
import com.isc.common.security.keymanagement.KeyResolver;
import com.isc.common.security.keymanagement.KeyResolverImpl;
import com.isc.common.security.keymanagement.PublicKeyProvider;
import com.isc.common.security.keymanagement.PublicKeyProviderImpl;
import com.isc.common.security.vault.SecurityVaultPublicKeyProvider;
import com.isc.common.security.vault.SecurityVaultPublicKeyProviderImpl;
import com.isc.common.vault.jwt.VaultJwtKeyStore;
import com.isc.common.vault.kv.VaultKeyValueService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.security.PublicKey;

@AutoConfiguration
@ConditionalOnProperty(prefix = "common.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({SecurityProperties.class, SecurityCacheProperties.class})
public class CommonSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationService authorizationService() {
        return new AuthorizationServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JwtValidatorService.class)
    public AuthenticationService authenticationService(
            JwtValidatorService validator) {
        return new AuthenticationServiceImpl(validator);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "common.security.cache.public-keys",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public Cache<String, PublicKey> securityPublicKeyCache(SecurityCacheProperties properties) {
        return Caffeine.newBuilder()
                .maximumSize(properties.getMaximumSize())
                .expireAfterWrite(properties.getTtl())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "common.security.cache.public-keys",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public SecurityKeyCache securityKeyCache(Cache<String, PublicKey> cache) {
        return new SecurityKeyCacheImpl(cache);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "common.security.cache.public-keys",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public SecurityVaultPublicKeyProvider securityVaultPublicKeyProvider(
            VaultJwtKeyStore keyStore,
            VaultKeyValueService keyValueService,
            SecurityProperties properties) {
        return new SecurityVaultPublicKeyProviderImpl(keyStore, keyValueService, properties);
    }

    @Bean
    @ConditionalOnMissingBean(PublicKeyProvider.class)
    @ConditionalOnBean(SecurityVaultPublicKeyProvider.class)
    public PublicKeyProvider publicKeyProvider(
            SecurityKeyCache cache,
            SecurityVaultPublicKeyProvider vaultProvider) {
        return new PublicKeyProviderImpl(cache, vaultProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(PublicKeyProvider.class)
    public KeyResolver keyResolver(PublicKeyProvider provider) {
        return new KeyResolverImpl(provider);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(KeyResolver.class)
    @ConditionalOnProperty(
            prefix = "common.security.jwt",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public JwtValidatorService jwtValidatorService(
            KeyResolver resolver,
            SecurityProperties properties) {
        return new JwtValidatorServiceImpl(resolver, properties);
    }
}
