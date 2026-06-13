package com.isc.common.constants;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    // =====================================================
    // AUTH DOMAIN
    // =====================================================

    public static final String AUTH_LOGIN =
            "auth.login.success";

    public static final String AUTH_LOGOUT =
            "auth.logout.success";

    public static final String AUTH_TOKEN_REFRESH =
            "auth.token.refresh";

    public static final String AUTH_TOKEN_REVOKED =
            "auth.token.revoked";

    public static final String AUTH_ACCOUNT_LOCKED =
            "auth.account.locked";

    public static final String AUTH_ACCOUNT_UNLOCKED =
            "auth.account.unlocked";


    // =====================================================
    // SESSION DOMAIN
    // =====================================================

    public static final String SESSION_CONNECTED =
            "session.connected";

    public static final String SESSION_DISCONNECTED =
            "session.disconnected";

    public static final String SESSION_EXPIRED =
            "session.expired";

    public static final String SESSION_KICKED =
            "session.kicked";

    public static final String SESSION_FORCE_LOGOUT =
            "session.force.logout";


    // =====================================================
    // DEVICE DOMAIN
    // =====================================================

    public static final String DEVICE_REGISTERED =
            "device.registered";

    public static final String DEVICE_UNREGISTERED =
            "device.unregistered";

    public static final String DEVICE_BLOCKED =
            "device.blocked";

    public static final String DEVICE_UNBLOCKED =
            "device.unblocked";

    public static final String DEVICE_CHANGED =
            "device.changed";


    // =====================================================
    // MQTT DOMAIN
    // =====================================================

    public static final String MQTT_CONNECTED =
            "mqtt.client.connected";

    public static final String MQTT_DISCONNECTED =
            "mqtt.client.disconnected";

    public static final String MQTT_SUBSCRIBED =
            "mqtt.client.subscribed";

    public static final String MQTT_UNSUBSCRIBED =
            "mqtt.client.unsubscribed";

    public static final String MQTT_MESSAGE_RECEIVED =
            "mqtt.message.received";

    public static final String MQTT_MESSAGE_DELIVERED =
            "mqtt.message.delivered";


    // =====================================================
    // TRANSACTION DOMAIN
    // =====================================================

    public static final String TRANSACTION_CREATED =
            "transaction.created";

    public static final String TRANSACTION_UPDATED =
            "transaction.updated";

    public static final String TRANSACTION_COMPLETED =
            "transaction.completed";

    public static final String TRANSACTION_REVERSED =
            "transaction.reversed";

    public static final String TRANSACTION_FAILED =
            "transaction.failed";


    // =====================================================
    // PAYMENT DOMAIN
    // =====================================================

    public static final String PAYMENT_CREATED =
            "payment.created";

    public static final String PAYMENT_COMPLETED =
            "payment.completed";

    public static final String PAYMENT_FAILED =
            "payment.failed";

    public static final String PAYMENT_REVERSED =
            "payment.reversed";


    // =====================================================
    // TRANSFER DOMAIN
    // =====================================================

    public static final String TRANSFER_CREATED =
            "transfer.created";

    public static final String TRANSFER_COMPLETED =
            "transfer.completed";

    public static final String TRANSFER_FAILED =
            "transfer.failed";

    public static final String TRANSFER_REVERSED =
            "transfer.reversed";


    // =====================================================
    // NOTIFICATION DOMAIN
    // =====================================================

    public static final String NOTIFICATION_CREATED =
            "notification.created";

    public static final String NOTIFICATION_SENT =
            "notification.sent";

    public static final String NOTIFICATION_DELIVERED =
            "notification.delivered";

    public static final String NOTIFICATION_FAILED =
            "notification.failed";


    // =====================================================
    // FRAUD DOMAIN
    // =====================================================

    public static final String FRAUD_DETECTED =
            "fraud.detected";

    public static final String FRAUD_BLOCKED =
            "fraud.blocked";

    public static final String FRAUD_REVIEW_REQUIRED =
            "fraud.review.required";


    // =====================================================
    // AUDIT DOMAIN
    // =====================================================

    public static final String AUDIT_EVENT =
            "audit.event";
}