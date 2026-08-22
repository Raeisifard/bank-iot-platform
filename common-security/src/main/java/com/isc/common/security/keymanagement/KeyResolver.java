package com.isc.common.security.keymanagement;

import java.security.PublicKey;

public interface KeyResolver {
    PublicKey resolve(String keyId);
}
