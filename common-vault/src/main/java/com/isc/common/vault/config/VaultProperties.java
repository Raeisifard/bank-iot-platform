package com.isc.common.vault.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "common.vault")
public class VaultProperties {

    @Setter
    private boolean enabled = false;
    @Setter
    private String uri = "http://localhost:8200";
    @Setter
    private String token = "";

    private final Kv kv = new Kv();
    private final Transit transit = new Transit();

    @Setter
    @Getter
    public static class Kv {
        private boolean enabled = true;
        private String mount = "key-management";

    }

    @Setter
    @Getter
    public static class Transit {
        private boolean enabled = true;
        private String mount = "transit";

    }
}
