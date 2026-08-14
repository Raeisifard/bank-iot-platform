package com.isc.infrastructure.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "infrastructure.kafka")
public class KafkaInfrastructureProperties {
    private boolean enabled = false;
    private String bootstrapServers;
    private String clientId = "bank-iot-platform";
    private String acks = "all";
    private boolean enableIdempotence = true;
    private int retries = 10;
    private int maxInFlightRequestsPerConnection = 5;
    private String consumerGroup;
    private boolean autoStartup = true;
}
