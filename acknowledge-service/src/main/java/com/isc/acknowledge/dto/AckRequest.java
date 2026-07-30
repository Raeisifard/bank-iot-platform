package com.isc.acknowledge.dto;

import com.isc.acknowledge.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/*{
        "transactionId": "TX-20260522-998877",
        "traceId": "TRACE-a8b91f3c",
        "messageId": "MSG-774411",
        "customerId": "CUST-100200",
        "deviceId": "DEVICE-ANDROID-01",
        "clientId": "mobile-989121234567",
        "deliveryAttempt": 2,
        "deliveredAt": "2026-05-22T11:20:15Z",
        "ackAt": "2026-05-22T11:20:17Z",
        "status": "PROCESSED",
        "failureReason": null,
        "jwtId": "0f5f0dcb-2f6c-4d73-bf59-4d1d6d53f1b7",
        "sessionId": "8c4d6d4f-b44b-49f0-bfd2-fb6a08dc1b11",
        "appVersion": "2.4.1",
        "platform": "ANDROID",
        "ipAddress": "10.20.1.15"
        }*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AckRequest {

    /*
     * transaction identifiers
     */
    private String transactionId;

    private String traceId;

    private String messageId;

    /*
     * customer/device
     */
    private String customerId;

    private String deviceId;

    private String clientId;

    /*
     * mqtt delivery metadata
     */
    private Integer deliveryAttempt;

    private Instant deliveredAt;

    private Instant ackAt;

    /*
     * application status
     */
    private MessageStatus status;
    // RECEIVED
    // PROCESSED
    // FAILED

    private String failureReason;

    /*
     * security
     */
    private String jwtId;

    private String sessionId;

    /*
     * app metadata
     */
    private String appVersion;

    private String platform;
    // ANDROID / IOS

    private String ipAddress;
}