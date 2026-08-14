package com.isc.sessionmanager.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures:
 *  - a JSON-aware consumer factory (tolerant of malformed messages via ErrorHandlingDeserializer)
 *  - a DefaultErrorHandler that retries transient failures a bounded number of times,
 *    then routes the poison message to a dead-letter topic instead of blocking the partition.
 *
 * IMPORTANT: the ingress producer (EmqxWebhookController) publishes BOTH
 * ClientConnectedEvent and ClientDisconnectedEvent to the same topic
 * (KafkaTopics.MQTT_CONNECTION), using Spring's default JsonSerializer, which
 * stamps a __TypeId__ header per record with the event's fully-qualified
 * class name. This consumer MUST honor that header (USE_TYPE_INFO_HEADERS=true)
 * rather than forcing every record to a single default type — otherwise
 * disconnect events silently get decoded as connect events and the
 * ClientDisconnectedEvent branch in ConnectionEventListener never fires.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${app.kafka.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.kafka.retry.backoff-ms:1000}")
    private long backoffMs;

    @Bean
    public ConsumerFactory<String, Object> connectionEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        // Real event package (was "com.isc.sessionmanager.model", which doesn't exist —
        // every message was being rejected as untrusted and sent straight to the DLT).
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.isc.contract.event.session");
        // Honor the __TypeId__ header the producer already sends per-record instead of
        // forcing a single default type; lets ClientConnectedEvent and
        // ClientDisconnectedEvent both deserialize correctly off the same topic.
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, Object> dltProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> dltKafkaTemplate(ProducerFactory<String, Object> dltProducerFactory) {
        return new KafkaTemplate<>(dltProducerFactory);
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> dltKafkaTemplate,
                                                  @Value("${app.kafka.topic.dead-letter}") String dltTopic) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dltKafkaTemplate,
                (record, ex) -> new org.apache.kafka.common.TopicPartition(dltTopic, record.partition())
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(backoffMs, maxAttempts - 1L)
        );
        // Don't retry on deserialization/validation errors — those will never succeed on retry.
        errorHandler.addNotRetryableExceptions(
                org.springframework.kafka.support.serializer.DeserializationException.class,
                jakarta.validation.ConstraintViolationException.class,
                IllegalArgumentException.class
        );
        return errorHandler;
    }

    @Bean
    public org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory<String, Object>
    connectionEventKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> connectionEventConsumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {

        var factory = new org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(connectionEventConsumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
