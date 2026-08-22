package com.isc.common.vault.kv;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.common.vault.config.VaultProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.support.Versioned;

@Service
@ConditionalOnBean(VaultTemplate.class)
public class VaultKeyValueServiceImpl implements VaultKeyValueService {

    private final VaultTemplate vaultTemplate;
    private final VaultProperties properties;
    private final ObjectMapper objectMapper;

    public VaultKeyValueServiceImpl(
            VaultTemplate vaultTemplate,
            VaultProperties properties,
            ObjectMapper objectMapper) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T read(String path, Class<T> type) {
        return read(properties.getKv().getMount(), path, type);
    }

    @Override
    public <T> T read(String mount, String path, Class<T> type) {
        requireKvEnabled();

        VaultVersionedKeyValueOperations kv =
                vaultTemplate.opsForVersionedKeyValue(mount);

        Versioned<Map<String, Object>> response = kv.get(path);

        if (response == null || response.getData() == null) {
            return null;
        }

        return objectMapper.convertValue(response.getData(), type);
    }

    @Override
    public void write(String path, Object value) {
        write(properties.getKv().getMount(), path, value);
    }

    @Override
    public void write(String mount, String path, Object value) {
        requireKvEnabled();

        VaultVersionedKeyValueOperations kv =
                vaultTemplate.opsForVersionedKeyValue(mount);

        Map<String, Object> data = objectMapper.convertValue(
                value,
                new TypeReference<Map<String, Object>>() {}
        );

        kv.put(path, data);
    }

    private void requireKvEnabled() {
        if (!properties.getKv().isEnabled()) {
            throw new IllegalStateException("Vault KV is disabled");
        }
    }
}
