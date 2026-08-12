package com.isc.common.constants;

public final class RedisKeys {

    private RedisKeys() {
    }

    public static final String SESSION = "session:";
    public static final String DEVICE_SESSION = "session:device:";
    public static final String CLIENT_SESSION = "session:client:";

    public static final String REFRESH_TOKEN = "refresh:";
    public static final String SESSION_REFRESH = "session:refresh:";

    // =====================================================
    // MESSAGE DELIVERY
    // =====================================================

    public static final String MESSAGE_DELIVERY = "delivery:";

    public static final String MESSAGE_RETRY = "delivery:retry";

    public static final String MESSAGE_PENDING = "pending:";
}