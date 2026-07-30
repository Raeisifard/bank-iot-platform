package com.isc.acknowledge.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RetryRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("kafka:banking.retry.2s")
                .delay(2000)
                .to("kafka:banking.tx.in");

        from("kafka:banking.retry.5s")
                .delay(5000)
                .to("kafka:banking.tx.in");
    }
}
