package com.isc.common.enums;

import lombok.Getter;

public enum JwtTokenType {

    // General purpose access token used to call protected APIs, short-lived
    ACCESS_TOKEN("ACT", false, true,
            "General access token for calling protected services"),

    // Used to obtain a new access token, one-time-use with rotation to prevent replay/theft
    REFRESH_TOKEN("RFT", true, false,
            "Session refresh token, one-time-use with rotation"),

    // Issued only for the login/authentication step, not valid for any other API call
    AUTHENTICATION_TOKEN("AUT", false, true,
            "Token issued strictly for the authentication step"),

    // Issued after password verification, valid only to pass the OTP verification step
    OTP_VERIFICATION_TOKEN("OTP", true, true,
            "OTP verification token, one-time-use and short-lived"),

    // Required for approving transactions above a configured high-value threshold
    HIGH_VALUE_TRANSACTION_TOKEN("HVT", true, true,
            "Approval token required for high-value transactions"),

    // Confirms one specific transaction, usually issued right after OTP verification
    TRANSACTION_CONFIRMATION_TOKEN("TXC", true, true,
            "Final confirmation token bound to a specific transaction"),

    // Used during the forgot-password / reset-password flow
    PASSWORD_RESET_TOKEN("PRT", true, true,
            "Password reset token, one-time-use and short-lived"),

    // Used to verify a user's mobile number or email during registration or profile update
    CONTACT_VERIFICATION_TOKEN("CVT", true, true,
            "Mobile number / email verification token"),

    // Issued when a user logs in from a new device and must confirm/register it
    DEVICE_REGISTRATION_TOKEN("DRT", true, true,
            "Confirms and registers a new user device"),

    // Step-up authentication token required before sensitive operations (e.g. adding a beneficiary)
    STEP_UP_AUTH_TOKEN("SUA", true, true,
            "Step-up authentication token for sensitive operations"),

    // Server-side session identifier, no strict short TTL but can be revoked manually
    SESSION_TOKEN("SES", false, false,
            "Server-side session token, revocable on demand"),

    // Machine-to-machine token for internal services, payment gateways, or third-party apps
    SERVICE_TO_SERVICE_TOKEN("S2S", false, true,
            "Machine-to-machine token for service integrations"),

    // Required for sensitive card operations such as blocking a card or changing card PIN
    CARD_MANAGEMENT_TOKEN("CMT", true, true,
            "Token required for sensitive card management operations"),

    // Reference token used to mass-revoke other tokens of a user/session (e.g. on breach detection or logout)
    REVOCATION_TOKEN("REV", true, false,
            "Reference token used to bulk-revoke a user's other tokens");

    @Getter
    private final String prefix;
    @Getter
    private final boolean oneTimeUse;
    @Getter
    private final boolean shortLived;
    @Getter
    private final String description;

    JwtTokenType(String prefix, boolean oneTimeUse, boolean shortLived, String description) {
        this.prefix = prefix;
        this.oneTimeUse = oneTimeUse;
        this.shortLived = shortLived;
        this.description = description;
    }

    /**
     * Builds the jti (JWT ID) value to embed in the token, e.g. "ACT-3f9e2b1c-...".
     * This makes every token id self-descriptive for logging and Redis auditing.
     */
    public String buildJti(String randomPart) {
        return prefix + "-" + randomPart;
    }

    /**
     * Builds the Redis key used to store/blacklist a token, e.g. "jwt:ACT:3f9e2b1c-...".
     * Having the type prefix inside the key makes it trivial to scan/audit
     * issued or consumed tokens per type (e.g. `redis-cli KEYS "jwt:OTP:*"`).
     */
    public String buildRedisKey(String jti) {
        return "jwt:" + prefix + ":" + jti;
    }

}
