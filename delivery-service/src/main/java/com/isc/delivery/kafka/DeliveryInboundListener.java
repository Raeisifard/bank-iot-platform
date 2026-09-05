package com.isc.delivery.kafka;

import com.isc.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "delivery", name = {"enabled", "oracle.enabled"}, havingValue = "true")
@ConditionalOnProperty(prefix = "common.redis", name = "enabled", havingValue = "true")
public class DeliveryInboundListener {

    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "${delivery.kafka.inbound-topic}",
            groupId = "${delivery.kafka.consumer-group}")
    public void consume(ConsumerRecord<String, String> record) {
        deliveryService.receive(record);
    }
}
