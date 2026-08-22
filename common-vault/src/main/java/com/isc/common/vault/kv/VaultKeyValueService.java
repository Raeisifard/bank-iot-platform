package com.isc.common.vault.kv;

public interface VaultKeyValueService {

    <T> T read(String path, Class<T> type);

    <T> T read(String mount, String path, Class<T> type);

    void write(String path, Object value);

    void write(String mount, String path, Object value);
}
