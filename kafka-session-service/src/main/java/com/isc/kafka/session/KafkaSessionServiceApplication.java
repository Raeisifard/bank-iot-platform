package com.isc.kafka.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = "com.isc")
@EnableKafka
public class KafkaSessionServiceApplication  {
    public static void main(String[] args) {
        SpringApplication.run(KafkaSessionServiceApplication .class, args);
    }
}
