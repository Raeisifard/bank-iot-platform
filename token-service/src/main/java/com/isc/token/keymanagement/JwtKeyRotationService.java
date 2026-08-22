package com.isc.token.keymanagement;

public interface JwtKeyRotationService {
    JwtKeyVersion rotate();
}
