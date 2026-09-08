package com.isc.useridentity.provider;
public interface BiometricProvider { boolean verify(String userId,String clientId,String assertion); }
