package com.isc.sessionmanager.kafka;

import com.isc.common.constants.KafkaTopics;
import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.sessionmanager.service.SessionService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Consumes client connection lifecycle events published by the Kafka Ingress
 * Service (topic: app.kafka.topic.connection-events, default
 * "client-connection-events").
 *
 * Acknowledgment is manual and only committed after the session mutation
 * succeeds, so a processing failure leaves the offset uncommitted and the
 * message gets redelivered / routed to the DLT per the configured
 * DefaultErrorHandler (see KafkaConsumerConfig).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectionEventListener {

    private final SessionService sessionService;
    private final Validator validator;

    @KafkaListener(
            //topics = "${app.kafka.topic.connection-events}",
            topics = KafkaTopics.MQTT_CONNECTED,
            containerFactory = "connectionEventKafkaListenerContainerFactory"
    )
    public void onConnectionEvent(ConsumerRecord<String, ClientConnectedEvent> record, Acknowledgment ack) {
        ClientConnectedEvent event = record.value();

        try {
            validate(event);
            log.debug("Received connection event: key={} partition={} offset={} event={}",
                    record.key(), record.partition(), record.offset(), event);

            sessionService.handleConnectionEvent(event);

            ack.acknowledge();
        } catch (ConstraintViolationException validationEx) {
            // Not retryable — will be routed straight to the DLT by the error handler.
            log.warn("Invalid connection event, sending to DLT: partition={} offset={} reason={}",
                    record.partition(), record.offset(), validationEx.getMessage());
            throw validationEx;
        } catch (Exception ex) {
            // Transient failure (e.g. Redis temporarily unavailable) — rethrow so the
            // configured DefaultErrorHandler can retry with backoff before DLT routing.
            log.error("Failed to process connection event, will retry: partition={} offset={}",
                    record.partition(), record.offset(), ex);
            throw ex;
        }
    }

    private void validate(ClientConnectedEvent event) {
        Set<ConstraintViolation<ClientConnectedEvent>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ConstraintViolationException(message, violations);
        }
    }
}
