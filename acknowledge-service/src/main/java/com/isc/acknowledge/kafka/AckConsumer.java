package com.isc.acknowledge.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.acknowledge.dto.AckRequest;
import com.isc.acknowledge.service.AckRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AckConsumer {

    private final ObjectMapper objectMapper;
    private final AckRedisService ackRedisService;

    @KafkaListener(
            topics = "${app.kafka.topics.ack}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String payload) {

        try {

            AckRequest ack =
                    objectMapper.readValue(
                            payload,
                            AckRequest.class
                    );

            Instant ackAt =
                    ack.getAckAt() != null
                            ? ack.getAckAt()
                            : Instant.now();

            var result =
                    ackRedisService.acknowledge(
                            ack.getMessageId(),
                            ack.getClientId(),
                            ack.getStatus().name(),
                            ackAt
                    );

            switch (result) {

                case ACKED ->
                        log.debug(
                                "Message ACKED. messageId={}, clientId={}",
                                ack.getMessageId(),
                                ack.getClientId()
                        );

                case DUPLICATE ->
                        log.debug(
                                "Duplicate ACK ignored. messageId={}",
                                ack.getMessageId()
                        );

                case NOT_FOUND ->
                        log.warn(
                                "ACK for unknown message. messageId={}",
                                ack.getMessageId()
                        );

                case CLIENT_MISMATCH ->
                        log.error(
                                "ACK client mismatch. messageId={}, clientId={}",
                                ack.getMessageId(),
                                ack.getClientId()
                        );
            }

        } catch (Exception e) {

            log.error(
                    "Failed to process ACK event",
                    e
            );

            throw new IllegalStateException(
                    "ACK processing failed",
                    e
            );
        }
    }
}