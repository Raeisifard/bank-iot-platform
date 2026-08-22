package com.isc.common.vault.transit;

public interface VaultTransitService {

    String sign(String keyName, String input);

    String sign(String keyName, String input, Integer keyVersion);

    String sign(String mount, String keyName, String input, Integer keyVersion);

    String getPublicKey(String keyName, Integer keyVersion);

    String getPublicKey(String mount, String keyName, Integer keyVersion);
}
