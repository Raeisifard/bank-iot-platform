package com.isc.common.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties("common.security")
public class SecurityProperties {
    private boolean enabled = true;
    private Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class Jwt {
        private boolean enabled = true;
        private Duration clockSkew = Duration.ofSeconds(30);
        private String algorithm = "RS256";
        private String issuer;
        private List<String> audiences = new ArrayList<>();
        private String vaultKeyName = "bank-jwt-v2";
        private String policyPath = "jwt/key-policy";
        private String policyMount;
    }
}
