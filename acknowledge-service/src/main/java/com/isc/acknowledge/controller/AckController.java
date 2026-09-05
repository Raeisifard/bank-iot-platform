package com.isc.acknowledge.controller;

import com.isc.acknowledge.dto.AckRequest;
import com.isc.common.constants.KafkaTopics;
import com.isc.common.security.model.SecurityPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

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
            @AuthenticationPrincipal SecurityPrincipal principal) {

        String clientId = validateClientOwnership(request, principal);

        log.debug(
                "ACK accepted for publication. messageId={}, clientId={}",
                request.getMessageId(),
                clientId
        );

        kafkaTemplate.send(
                KafkaTopics.ACK_EVENT,
                request.getMessageId(),
                request
        );

        return ResponseEntity.accepted().build();
    }

    private String validateClientOwnership(
            AckRequest request,
            SecurityPrincipal principal) {

        if (principal == null || principal.getClientAttributes() == null
                || principal.getClientAttributes().getCid() == null
                || principal.getClientAttributes().getCid().isBlank()) {
            log.warn("ACK rejected: authenticated JWT has no client cid");
            throw new ResponseStatusException(
                    UNAUTHORIZED, "Authenticated client identity is missing");
        }

        String clientId = principal.getClientAttributes().getCid();
        if (request.getClientId() == null || !clientId.equals(request.getClientId())) {
            log.warn("ACK rejected: client cid mismatch. messageId={}, tokenCid={}, requestCid={}",
                    request.getMessageId(), clientId, request.getClientId());
            throw new ResponseStatusException(
                    FORBIDDEN, "ACK clientId does not match authenticated client");
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

                return clientId;
    }
}