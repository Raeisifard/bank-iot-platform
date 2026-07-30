package com.isc.security.vault;

import java.security.PublicKey;

public interface VaultService {

    PublicKey getPublicKey(String kid);
}