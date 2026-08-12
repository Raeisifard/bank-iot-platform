package com.isc.acknowledge.route;

import com.isc.acknowledge.dto.AckRequest;
import com.isc.acknowledge.processor.AckValidatorProcessor;
import com.isc.acknowledge.service.AckService;
import com.isc.common.constants.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AckRoute extends RouteBuilder {

    private final AckValidatorProcessor ackValidatorProcessor;

    private final AckService ackService;

    @Override
    public void configure() {

        from("kafka:" + KafkaTopics.ACK_EVENT)
                .routeId("ack-route")

                .unmarshal().json(AckRequest.class)

                .process(ackValidatorProcessor)

                .choice()
                .when(simple("${exchangeProperty.duplicateAck} == true"))
                .log("Duplicate ACK ignored: ${body.transactionId}")
                .otherwise()
                .process(exchange -> {

                    AckRequest ack = exchange.getProperty("ack", AckRequest.class);

                    ackService.handle(ack);

                    exchange.setProperty("txId", ack.getTransactionId());
                })
                .log("ACK processed: ${exchangeProperty.txId}")
                .end();
    }
}