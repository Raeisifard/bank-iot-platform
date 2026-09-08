package com.isc.useridentity.provider;

/** Future abstraction for Android/iOS TEE, passkey/WebAuthn and device-key assertions. */
public interface DeviceAssertionProvider {
    boolean verify(String userId, String clientId, String challenge, String assertion);
}
