package com.isc.delivery.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "delivery", name = {"enabled", "oracle.enabled"}, havingValue = "true")
@ConditionalOnProperty(prefix = "common.redis", name = "enabled", havingValue = "true")
public class DeliveryAcknowledgementListener {

    private final ObjectMapper objectMapper;
    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "${delivery.kafka.acknowledgement-topic}",
            groupId = "${delivery.kafka.consumer-group}-ack")
    public void consume(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            JsonNode messageId = node.get("messageId");
            if (messageId == null || messageId.asText().isBlank()) {
                throw new IllegalArgumentException("Acknowledgement messageId is required");
            }
            deliveryService.acknowledge(messageId.asText());
        } catch (Exception exception) {
            log.error("Could not process delivery acknowledgement", exception);
            throw new IllegalStateException("Acknowledgement processing failed", exception);
        }
    }
}
