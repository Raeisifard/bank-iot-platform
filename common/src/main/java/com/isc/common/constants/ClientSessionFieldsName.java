package com.isc.common.constants;

import lombok.NoArgsConstructor;

/**
 * Field names of the session hash stored in Redis under key "session:<sessionId>".
 * SESSION_ID is always jwt.sid — sessions are never looked up by phone number.
 */
@NoArgsConstructor
public class ClientSessionFieldsName {

    // Populated at authentication time (token-service)
    public static final String SESSION_ID = "sessionId";
    public static final String CUSTOMER_ID = "customerId";
    public static final String DEVICE_ID = "deviceId";
    public static final String CLIENT_ID = "clientId";
    public static final String REFRESH_TOKEN_ID = "refreshTokenId";
    public static final String CREATED_AT = "createdAt";
    public static final String EXPIRE_AT = "expireAt";
    public static final String LAST_REFRESH_AT = "lastRefreshAt";
    public static final String STATUS = "status";
    public static final String REASON = "reason";

    // Populated/refreshed by connection lifecycle events (session-manager)
    public static final String USERNAME = "username";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String NODE = "node";
    public static final String PROTOCOL = "protocol";
    public static final String LAST_EVENT_TIMESTAMP = "lastEventTimestamp";
    public static final String UPDATED_AT = "updatedAt";

    // Deprecated: no longer used as a lookup key, kept only if legacy records still carry it
    @Deprecated
    public static final String PHONE_NO = "phoneNo";
}
