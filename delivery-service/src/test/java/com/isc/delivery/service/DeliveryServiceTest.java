package com.isc.delivery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.isc.delivery.config.DeliveryProperties;
import com.isc.delivery.model.DeliveryRecord;
import com.isc.delivery.repository.OracleDeliveryStore;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private OracleDeliveryStore store;

    @Mock
    private DeliveryRedisState redisState;

    private DeliveryService service;

    @BeforeEach
    void setUp() {
        DeliveryProperties properties = new DeliveryProperties();
        properties.setEnabled(true);
        properties.getOracle().setEnabled(true);
        service = new DeliveryService(
            new ObjectMapper().registerModule(new JavaTimeModule()),
            kafkaTemplate, store, redisState, properties);
    }

    @Test
    void receivePersistsTracksAndPublishesMessage() {
        CompletableFuture future = CompletableFuture.completedFuture(null);
        org.mockito.Mockito.when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(future);

        service.receive(new ConsumerRecord<>("delivery.inbound", 0, 10L,
                "client-1", "{\"message\":\"hello\",\"clientId\":\"client-1\"}"));

        verify(store).insertPending(any(DeliveryRecord.class));
        verify(redisState).pending(any(DeliveryRecord.class));
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
        verify(redisState).scheduleRetry(anyString(), any());
    }
}
