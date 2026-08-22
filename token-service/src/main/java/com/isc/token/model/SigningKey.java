package com.isc.token.model;

import java.security.PrivateKey;
import java.security.PublicKey;

public record SigningKey(String keyId, PrivateKey privateKey, PublicKey publicKey) {
}
