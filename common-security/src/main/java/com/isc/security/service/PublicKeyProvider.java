package com.isc.security.service;

import java.security.PublicKey;

public interface PublicKeyProvider {

    PublicKey getPublicKey(String kid);
}
