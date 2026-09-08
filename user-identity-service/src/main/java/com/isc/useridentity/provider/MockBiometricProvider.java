package com.isc.useridentity.provider;
import org.springframework.context.annotation.Profile; import org.springframework.stereotype.Component;
@Component @Profile("!prod") public class MockBiometricProvider implements BiometricProvider { public boolean verify(String userId,String clientId,String assertion){ return "MOCK-BIOMETRIC-OK".equals(assertion); } }
