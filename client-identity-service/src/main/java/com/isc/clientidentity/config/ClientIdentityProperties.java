package com.isc.clientidentity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "client-identity")
public class ClientIdentityProperties {
    private Duration challengeTtl = Duration.ofMinutes(2);

    public Duration getChallengeTtl() {
        return challengeTtl;
    }

    public void setChallengeTtl(Duration challengeTtl) {
        this.challengeTtl = challengeTtl;
    }
}