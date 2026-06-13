package com.isc.tokenservice.enums;

public enum SessionReason {
    NONE,
    USER_LOGOUT,
    REFRESH_REUSE,
    PASSWORD_CHANGED,
    DEVICE_REMOVED,
    ADMIN_REVOKED,
    SECURITY_INCIDENT
}