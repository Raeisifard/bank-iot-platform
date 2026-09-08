package com.isc.useridentity.provider;
public interface OtpDeliveryProvider { void deliver(String userId,String destination,String code,String purpose); }
