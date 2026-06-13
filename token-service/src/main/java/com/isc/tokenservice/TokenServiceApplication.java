package com.isc.tokenservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class TokenServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TokenServiceApplication.class, args);
    }
}
