package com.isc.delivery.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.delivery.config.DeliveryProperties;
import com.isc.delivery.model.DeliveryEnvelope;
import com.isc.delivery.model.DeliveryRecord;
import com.isc.delivery.model.DeliveryState;
import com.isc.delivery.repository.OracleDeliveryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "delivery", name = {"enabled", "oracle.enabled"}, havingValue = "true")
@ConditionalOnProperty(prefix = "common.redis", name = "enabled", havingValue = "true")
public class DeliveryService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OracleDeliveryStore store;
    private final DeliveryRedisState redisState;
    private final DeliveryProperties properties;

    public void receive(ConsumerRecord<String, String> record) {
        String messageId = messageId(record);
        String clientId = clientId(record.value());
        Instant now = Instant.now();
        DeliveryRecord delivery = new DeliveryRecord(
                messageId, clientId, record.value(), 0, DeliveryState.PENDING, now, now, null);

        store.insertPending(delivery);
        redisState.pending(delivery);
        publish(delivery);
        scheduleNext(delivery);
    }

    public void retry(String messageId) {
        Optional<DeliveryRecord> optional = store.find(messageId);
        if (optional.isEmpty() || redisState.state(messageId).orElse(DeliveryState.ARCHIVED) == DeliveryState.DELIVERED) {
            redisState.removeRetry(messageId);
            return;
        }

        DeliveryRecord current = optional.get();
        if (current.attempt() >= properties.getRetry().getDelays().size()) {
            fail(current, DeliveryState.ARCHIVED, "Acknowledgement was not received");
            return;
        }

        DeliveryRecord retry = new DeliveryRecord(
                current.messageId(), current.clientId(), current.payload(), current.attempt() + 1,
                DeliveryState.PENDING, current.createdAt(), Instant.now(), null);
        store.updateAttempt(retry.messageId(), retry.attempt(), retry.updatedAt());
        redisState.pending(retry);
        publish(retry);
        scheduleNext(retry);
    }

    public void acknowledge(String messageId) {
        Optional<DeliveryRecord> optional = store.find(messageId);
        if (optional.isEmpty()) {
            log.warn("Acknowledgement received for unknown messageId={}", messageId);
            return;
        }
        DeliveryRecord record = optional.get();
        if (record.state() == DeliveryState.DELIVERED) {
            return;
        }
        Instant now = Instant.now();
        store.markDelivered(messageId, now);
        redisState.delivered(new DeliveryRecord(
                record.messageId(), record.clientId(), record.payload(), record.attempt(),
                DeliveryState.DELIVERED, record.createdAt(), now, null));
    }

    private void publish(DeliveryRecord record) {
        try {
            String payload = objectMapper.writeValueAsString(new DeliveryEnvelope(
                    record.messageId(), record.clientId(), record.payload(), record.attempt(), record.createdAt()));
            kafkaTemplate.send(properties.getKafka().getOutboundTopic(), record.messageId(), payload)
                    .get(properties.getRetry().getPublishTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            log.error("Could not publish delivery messageId={}", record.messageId(), exception);
            throw new IllegalStateException("Delivery publish failed", exception);
        }
    }

    private void scheduleNext(DeliveryRecord record) {
        int delayIndex = record.attempt();
        var delays = properties.getRetry().getDelays();
        if (delays.isEmpty()) {
            fail(record, DeliveryState.ARCHIVED, "No retry delay is configured");
            return;
        }
        int scheduledDelayIndex = Math.min(delayIndex, delays.size() - 1);
        redisState.scheduleRetry(
                record.messageId(),
                Instant.now().plus(delays.get(scheduledDelayIndex)));
    }

    private void fail(DeliveryRecord record, DeliveryState state, String reason) {
        Instant now = Instant.now();
        store.markFailed(record.messageId(), state, reason, now);
        redisState.failed(record, state,
                state == DeliveryState.ARCHIVED ? properties.getAudit().getArchivedTtl() : properties.getAudit().getFailedTtl());
    }

    private String messageId(ConsumerRecord<String, String> record) {
        String header = header(record, "messageId");
        if (header != null && !header.isBlank()) {
            return header;
        }
        if (record.key() != null && !record.key().isBlank()) {
            return record.key();
        }
        return UUID.nameUUIDFromBytes((record.topic() + ':' + record.partition() + ':' + record.offset())
                .getBytes(StandardCharsets.UTF_8)).toString();
    }

    private String clientId(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            JsonNode clientId = node.get("clientId");
            return clientId == null || clientId.isNull() ? "unknown" : clientId.asText();
        } catch (JsonProcessingException exception) {
            return "unknown";
        }
    }

    private String header(ConsumerRecord<String, String> record, String name) {
        var header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
