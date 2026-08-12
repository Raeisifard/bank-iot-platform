package com.isc.acknowledge.route;

import com.isc.common.constants.KafkaTopics;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RetryRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("kafka:" + KafkaTopics.RETRY_2S_EVENT)
                .delay(2000)
                .to("kafka:" + KafkaTopics.TX_IN_EVENT);

        from("kafka:" + KafkaTopics.RETRY_5S_EVENT)
                .delay(5000)
                .to("kafka:" + KafkaTopics.TX_IN_EVENT);
    }
}
