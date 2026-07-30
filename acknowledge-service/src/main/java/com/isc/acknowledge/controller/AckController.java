package com.isc.acknowledge.controller;

import com.isc.acknowledge.dto.AckRequest;
import com.isc.acknowledge.service.RedisPendingService;
import com.isc.security.model.JwtClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/acks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Acknowledge")
public class AckController {

    private final RedisPendingService pendingService;
    private final KafkaTemplate<String, AckRequest> kafkaTemplate;

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Delete Transaction")
    public ResponseEntity<String> deleteAck(
            @PathVariable String messageId,
            @AuthenticationPrincipal JwtClaims claims) {

        log.info(
                "ACK received. messageId={}, clientId={}",
                messageId,
                claims.getClientId());

        pendingService.removePending(messageId);

        return ResponseEntity.ok(
                "ACK received: " + messageId);
    }

    @PostMapping
    @Operation(summary = "Send ACK to Kafka")
    public void publishAck(
            @RequestBody AckRequest request,
            @AuthenticationPrincipal JwtClaims claims) {

        log.info(
                "Publish ACK. clientId={}",
                claims.getClientId());

        kafkaTemplate.send(
                "banking.ack",
                request);
    }

    @GetMapping("/ack")
    public ResponseEntity<String> ack(
            Authentication authentication) {

        JwtClaims claims =
                (JwtClaims) authentication.getPrincipal();

        String clientId =
                claims.getClientId();

        return ResponseEntity.ok(clientId);
    }
}