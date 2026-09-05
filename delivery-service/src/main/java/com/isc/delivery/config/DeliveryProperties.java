package com.isc.delivery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "delivery")
public class DeliveryProperties {

    private boolean enabled;
    private String serviceName = "delivery-service";
    private Kafka kafka = new Kafka();
    private Oracle oracle = new Oracle();
    private Retry retry = new Retry();
    private Audit audit = new Audit();

    @Data
    public static class Kafka {
        private String inboundTopic = "delivery.inbound";
        private String outboundTopic = "mqtt.message.send";
        private String acknowledgementTopic = "ack.event";
        private String consumerGroup = "delivery-service";
    }

    @Data
    public static class Oracle {
        private boolean enabled;
        private String url;
        private String username;
        private String password;
        private String tableName = "IOT_DELIVERY_MESSAGE";
    }

    @Data
    public static class Retry {
        private List<Duration> delays = List.of(Duration.ofSeconds(2), Duration.ofSeconds(5));
        private Duration publishTimeout = Duration.ofSeconds(10);
        private Duration schedulerInterval = Duration.ofSeconds(1);
    }

    @Data
    public static class Audit {
        private Duration deliveredTtl = Duration.ofDays(30);
        private Duration failedTtl = Duration.ofDays(90);
        private Duration archivedTtl = Duration.ofDays(180);
        private Duration pendingTtl = Duration.ofDays(7);
    }
}
