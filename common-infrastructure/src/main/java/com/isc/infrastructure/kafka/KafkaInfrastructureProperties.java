package com.isc.infrastructure.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "infrastructure.kafka")
public class KafkaInfrastructureProperties {

    private boolean enabled = false;

    private String bootstrapServers;

    private String clientId = "bank-iot-platform";

    private Producer producer = new Producer();

    private Consumer consumer = new Consumer();

    private Listener listener = new Listener();

    @Data
    public static class Producer {

        private String acks = "all";

        private boolean enableIdempotence = true;

        private int retries = 10;

        private int maxInFlightRequestsPerConnection = 5;

        private String compressionType;

    }

    @Data
    public static class Consumer {

        private String groupId;

        private String autoOffsetReset = "earliest";

        private boolean enableAutoCommit = false;

        private String keyDeserializer =
                "org.apache.kafka.common.serialization.StringDeserializer";

        private String valueDeserializer =
                "org.apache.kafka.common.serialization.StringDeserializer";

        private int maxPollRecords = 500;

        private int maxPollIntervalMs = 300000;

    }

    @Data
    public static class Listener {

        private boolean autoStartup = true;

        private String ackMode = "manual_immediate";

        private int concurrency = 1;

        private boolean missingTopicsFatal = false;

    }
}