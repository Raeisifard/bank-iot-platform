package com.isc.acknowledge.controller;

import com.isc.acknowledge.dto.AckRequest;
import com.isc.acknowledge.enums.MessageStatus;
import com.isc.common.dto.ClientAttributes;
import com.isc.common.security.model.SecurityPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AckControllerTest {

    private KafkaTemplate<String, AckRequest> kafkaTemplate;
    private AckController controller;
    private SecurityPrincipal principal;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        controller = new AckController(kafkaTemplate);

        ClientAttributes attributes = new ClientAttributes();
        attributes.setCid("client-1");
        principal = new SecurityPrincipal(
                "subject-1", Set.of(), Map.of(), attributes);
    }

    @Test
    void publishesAckWhenRequestClientMatchesTokenCid() {
        AckRequest request = requestFor("client-1");

        controller.acknowledge(request, principal);

        verify(kafkaTemplate).send(eq("ack.event"), eq("message-1"), eq(request));
    }

    @Test
    void rejectsRequestWhenClientDoesNotMatchTokenCid() {
        AckRequest request = requestFor("client-2");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.acknowledge(request, principal));

        assertEquals(403, exception.getStatusCode().value());
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void rejectsRequestWhenTokenClientIdentityIsMissing() {
        AckRequest request = requestFor("client-1");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.acknowledge(request, null));

        assertEquals(401, exception.getStatusCode().value());
        verifyNoInteractions(kafkaTemplate);
    }

    private AckRequest requestFor(String clientId) {
        return AckRequest.builder()
                .messageId("message-1")
                .clientId(clientId)
                .status(MessageStatus.PROCESSED)
                .build();
    }
}
