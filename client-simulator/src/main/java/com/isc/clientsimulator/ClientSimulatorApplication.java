package com.isc.clientsimulator;

import com.isc.clientsimulator.config.ClientSimulatorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ClientSimulatorProperties.class)
public class ClientSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientSimulatorApplication.class, args);
    }
}
