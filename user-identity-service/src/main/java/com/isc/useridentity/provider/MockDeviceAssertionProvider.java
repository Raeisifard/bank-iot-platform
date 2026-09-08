package com.isc.useridentity.provider;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component @Profile("!prod")
public class MockDeviceAssertionProvider implements DeviceAssertionProvider {
    @Override public boolean verify(String userId, String clientId, String challenge, String assertion) {
        return "MOCK-DEVICE-SIGNATURE-OK".equals(assertion);
    }
}
