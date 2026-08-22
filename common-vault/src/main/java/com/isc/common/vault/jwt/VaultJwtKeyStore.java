package com.isc.common.vault.jwt;

public interface VaultJwtKeyStore {

    String getPublicKey(String keyName, Integer keyVersion);

    String sign(String keyName, String input, Integer keyVersion);
}
