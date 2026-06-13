package com.isc.mqtt.ingress.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MQTT Ingress Service API",
                version = "1.0.0",
                description = "Receives MQTT/EMQX webhook events and forwards them to Kafka"
        )
)
public class OpenApiConfig {
}
