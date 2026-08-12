package com.isc.acknowledge.dto;

import com.isc.acknowledge.enums.MessageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AckRequest {

    @NotBlank
    private String messageId;

    private String transactionId;

    private String traceId;

    @NotBlank
    private String clientId;

    private String customerId;

    private String deviceId;

    private Integer deliveryAttempt;

    private Instant deliveredAt;

    private Instant ackAt;

    @NotNull
    private MessageStatus status;

    private String failureReason;

    private String jwtId;

    private String sessionId;

    private String appVersion;

    private String platform;

    private String ipAddress;
}