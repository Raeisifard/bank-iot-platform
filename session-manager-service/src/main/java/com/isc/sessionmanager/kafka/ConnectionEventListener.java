package com.isc.sessionmanager.kafka;

import com.isc.common.constants.KafkaTopics;
import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.contract.event.session.ClientDisconnectedEvent;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectionEventListener {

    private final SessionService sessionService;
    private final Validator validator;

    @KafkaListener(
            topics = KafkaTopics.MQTT_CONNECTION,
            containerFactory = "connectionEventKafkaListenerContainerFactory"
    )
    public void onConnectionEvent(
            ConsumerRecord<String, Object> record,
            Acknowledgment ack) {

        Object event = record.value();

        try {
            switch (event) {

                case ClientConnectedEvent connectedEvent -> {
                    validate(connectedEvent);

                    log.debug(
                            "Received CLIENT_CONNECTED: key={} partition={} offset={} sid={}",
                            record.key(),
                            record.partition(),
                            record.offset(),
                            connectedEvent.getJwt().getSid()
                    );

                    sessionService.handleConnectionEvent(connectedEvent);
                }

                case ClientDisconnectedEvent disconnectedEvent -> {
                    validate(disconnectedEvent);

                    log.debug(
                            "Received CLIENT_DISCONNECTED: key={} partition={} offset={} sid={}",
                            record.key(),
                            record.partition(),
                            record.offset(),
                            disconnectedEvent.getJwt().getSid()
                    );

                    sessionService.handleDisconnectionEvent(disconnectedEvent);
                }

                default -> {
                    log.warn(
                            "Unsupported connection event type: key={} partition={} offset={} class={}",
                            record.key(),
                            record.partition(),
                            record.offset(),
                            event != null ? event.getClass().getName() : "null"
                    );

                    throw new IllegalArgumentException(
                            "Unsupported connection event: "
                                    + (event != null
                                    ? event.getClass().getName()
                                    : "null")
                    );
                }
            }

            ack.acknowledge();

        } catch (ConstraintViolationException validationEx) {

            // Not retryable — routed to DLT by the configured error handler.
            log.warn(
                    "Invalid connection event, sending to DLT: partition={} offset={} reason={}",
                    record.partition(),
                    record.offset(),
                    validationEx.getMessage()
            );

            throw validationEx;

        } catch (Exception ex) {

            // Transient failure — rethrow so DefaultErrorHandler
            // can retry with the configured backoff.
            log.error(
                    "Failed to process connection event, will retry: partition={} offset={}",
                    record.partition(),
                    record.offset(),
                    ex
            );

            throw ex;
        }
    }

    private <T> void validate(T event) {

        Set<ConstraintViolation<T>> violations =
                validator.validate(event);

        if (!violations.isEmpty()) {

            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            throw new ConstraintViolationException(
                    message,
                    violations
            );
        }
    }
}
