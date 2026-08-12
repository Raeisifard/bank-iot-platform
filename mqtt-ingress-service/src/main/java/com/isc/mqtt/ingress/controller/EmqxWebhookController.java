package com.isc.mqtt.ingress.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.common.constants.KafkaTopics;
import com.isc.contract.event.KafkaEventFactory;
import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.contract.event.session.ClientDisconnectedEvent;
import com.isc.mqtt.ingress.dto.EmqxClientConnectedRequest;
import com.isc.mqtt.ingress.dto.EmqxClientDisconnectedRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(
        name = "EMQX Webhook",
        description = "Endpoints used by EMQX webhook integration"
)
public class EmqxWebhookController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaEventFactory kafkaEventFactory;

    @PostMapping("/{channel}/client/connected")
    @Operation(
            summary = "Client Connected Event",
            description = """
                    Receives EMQX client.connected webhook events and publishes
                    them to Kafka topic mqtt.client.connected.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Event received successfully"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request payload"
    )
    public ResponseEntity<Void> connected(
            @Parameter(
                    description = "EMQX Client Connected Event Payload",
                    required = true
            )
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Client connection event",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = EmqxClientConnectedRequest.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": 1781100207683,
                                              "client_id": "CUST-1002",
                                              "username": "behnam",
                                              "ip_address": "192.168.218.99:54071",
                                              "protocol": 5,
                                              "jwt": {
                                                "sid": "SID-123",
                                                "jti": "JTI-123",
                                                "iss": "bank-auth",
                                                "did": "DEVICE-1",
                                                "cid": "MOBILE-APP",
                                                "aud": "mobile-app"
                                              }
                                            }
                                            """
                            )
                    )
            )
            @RequestBody EmqxClientConnectedRequest request, @PathVariable String channel)
            throws JsonProcessingException {

        ClientConnectedEvent event =
                kafkaEventFactory
                        .clientConnected(
                                request.getJwt() != null
                                        ? request.getJwt().getSid()
                                        : request.getClientId())
                        .connectedAt(request.getConnectedAt())
                        .channel(channel)
                        //.actor(request.getJwt().getAud())
                        .clientId(request.getClientId())
                        .username(request.getUsername())
                        .ipAddress(request.getIpAddress())
                        .protocol(request.getProtocol())
                        .jwt(request.getJwt())
                        .build();

        log.info("EVENT =\n{}",
                objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(event));

        kafkaTemplate.send(
                KafkaTopics.MQTT_CONNECTION,
                event.getClientId(),
                event);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{channel}/client/disconnected")
    @Operation(
            summary = "Client Disconnected Event",
            description = """
                    Receives EMQX client.disconnected webhook events and publishes
                    them to Kafka topic mqtt.client.disconnected.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Event received successfully"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request payload"
    )
    public ResponseEntity<Void> disconnected(
            @Parameter(
                    description = "EMQX Client Disconnected Event Payload",
                    required = true
            )
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Client disconnection event",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = EmqxClientDisconnectedRequest.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": 1781100207683,
                                              "client_id": "CUST-1002",
                                              "username": "behnam",
                                              "ip_address": "192.168.218.99:54071",
                                              "protocol": 5,
                                              "jwt": {
                                                "sid": "SID-123",
                                                "jti": "JTI-123",
                                                "iss": "bank-auth",
                                                "did": "DEVICE-1",
                                                "cid": "MOBILE-APP",
                                                "aud": "mobile-app"
                                              }
                                            }
                                            """
                            )
                    )
            )
            @RequestBody EmqxClientDisconnectedRequest request, @PathVariable String channel)
            throws JsonProcessingException {

        ClientDisconnectedEvent event =
                kafkaEventFactory
                        .clientDisconnected(
                                request.getJwt() != null
                                        ? request.getJwt().getSid()
                                        : request.getClientId())
                        .connectedAt(request.getConnectedAt())
                        .disconnectedAt(request.getDisconnectedAt())
                        .channel(channel)
                        //.actor(request.getJwt().getAud())
                        .clientId(request.getClientId())
                        .username(request.getUsername())
                        .ipAddress(request.getIpAddress())
                        .protocol(request.getProtocol())
                        .jwt(request.getJwt())
                        .build();

        log.info("EVENT =\n{}",
                objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(event));

        kafkaTemplate.send(
                KafkaTopics.MQTT_CONNECTION,
                event.getClientId(),
                event);

        return ResponseEntity.ok().build();
    }

}