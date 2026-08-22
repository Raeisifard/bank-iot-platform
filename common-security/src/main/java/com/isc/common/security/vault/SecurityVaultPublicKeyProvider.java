package com.isc.common.security.vault;

import java.security.PublicKey;

public interface SecurityVaultPublicKeyProvider {
    PublicKey getPublicKey(String kid);

    void invalidate();
}
