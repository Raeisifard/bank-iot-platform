package com.isc.common.vault;

import com.isc.common.vault.config.VaultConfig;
import com.isc.common.vault.jwt.VaultJwtKeyStoreImpl;
import com.isc.common.vault.kv.VaultKeyValueServiceImpl;
import com.isc.common.vault.transit.VaultTransitServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        VaultConfig.class,
        VaultKeyValueServiceImpl.class,
        VaultTransitServiceImpl.class,
        VaultJwtKeyStoreImpl.class
})
@ConditionalOnProperty(
        prefix = "common.vault",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class CommonVaultAutoConfiguration {
}
