package com.isc.clientidentity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.isc.clientidentity.config.ClientIdentityProperties;

@SpringBootApplication
@EnableConfigurationProperties(ClientIdentityProperties.class)
public class ClientIdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientIdentityServiceApplication.class, args);
    }
}