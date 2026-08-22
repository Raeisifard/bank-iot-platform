package com.isc.token.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "token.jwt.key-policy")
public class JwtKeyPolicyProperties {

    private String keyName = "bank-jwt-v2";
    private String algorithm = "RS256";
    private Duration rotationInterval = Duration.ofDays(30);
    private Duration publicationOverlap = Duration.ofDays(7);
    private int maxPublishedVersions = 2;

    /**
     * KV v2 path containing active/inactive/next/retired JWT key metadata.
     */
    private String policyPath = "jwt/key-policy";

    /**
     * Optional KV mount. Blank means use common.vault.kv.mount.
     */
    private String policyMount = "";
}
