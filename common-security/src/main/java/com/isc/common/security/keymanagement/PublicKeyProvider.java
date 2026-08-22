package com.isc.common.security.keymanagement;

import java.security.PublicKey;

public interface PublicKeyProvider {
    PublicKey getPublicKey(String keyId);
}
