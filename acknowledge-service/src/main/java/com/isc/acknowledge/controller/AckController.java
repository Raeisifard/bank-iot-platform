package com.isc.acknowledge.controller;

import com.isc.acknowledge.dto.AckRequest;
import com.isc.common.constants.KafkaTopics;
import com.isc.common.dto.ClientAttributes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/acks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Acknowledge")
public class AckController {

    private final KafkaTemplate<String, AckRequest> kafkaTemplate;

    @PostMapping
    @Operation(
            summary = "Acknowledge a delivered message"
    )
    public ResponseEntity<Void> acknowledge(
            @Valid @RequestBody AckRequest request,
            @AuthenticationPrincipal ClientAttributes client) {

        validateClientOwnership(request, client);

        log.debug(
                "ACK accepted for publication. messageId={}, clientId={}",
                request.getMessageId(),
                client.getCid()
        );

        kafkaTemplate.send(
                KafkaTopics.ACK_EVENT,
                request.getMessageId(),
                request
        );

        return ResponseEntity.accepted().build();
    }

    private void validateClientOwnership(
            AckRequest request,
            ClientAttributes client) {

        if (client == null) {
            throw new IllegalStateException("Authenticated client is missing");
        }

        if (request.getClientId() == null ||
                !client.getCid().equals(request.getClientId())) {

            throw new IllegalArgumentException(
                    "ACK clientId does not match authenticated client"
            );
        }

        if (request.getMessageId() == null ||
                request.getMessageId().isBlank()) {

            throw new IllegalArgumentException(
                    "messageId is required"
            );
        }

        if (request.getStatus() == null) {
            throw new IllegalArgumentException(
                    "status is required"
            );
        }
    }
}