package com.isc.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "delivery", name = {"enabled", "oracle.enabled"}, havingValue = "true")
@ConditionalOnProperty(prefix = "common.redis", name = "enabled", havingValue = "true")
public class DeliveryRetryScheduler {

    private final DeliveryRedisState redisState;
    private final DeliveryService deliveryService;

    @Scheduled(fixedDelayString = "${delivery.retry.scheduler-interval-ms:1000}")
    public void retryDueMessages() {
        for (String messageId : redisState.dueRetries(Instant.now())) {
            try {
                deliveryService.retry(messageId);
            } catch (RuntimeException exception) {
                log.error("Retry failed for messageId={}", messageId, exception);
            }
        }
    }
}
