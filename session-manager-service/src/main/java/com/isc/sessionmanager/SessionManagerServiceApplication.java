package com.isc.sessionmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@ConfigurationPropertiesScan
public class SessionManagerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SessionManagerServiceApplication.class, args);
    }
}
