package com.isc.acknowledge.processor;

import com.isc.acknowledge.dto.AckRequest;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AckValidatorProcessor implements Processor {

    private final StringRedisTemplate redis;

    @Override
    public void process(Exchange exchange) {

        AckRequest ack =
                exchange.getMessage().getBody(AckRequest.class);

        if (ack == null) {
            throw new IllegalArgumentException("ACK is null");
        }

        if (ack.getTransactionId() == null ||
                ack.getTransactionId().isBlank()) {
            throw new IllegalArgumentException("Missing transactionId");
        }

        if (ack.getStatus() == null) {
            throw new IllegalArgumentException("Missing status");
        }

        // -----------------------------------------
        // idempotency check (VERY IMPORTANT)
        // -----------------------------------------

        String key = "ack:processed:" + ack.getTransactionId();

        Boolean alreadyProcessed = redis.hasKey(key);

        if (alreadyProcessed) {
            exchange.setProperty("duplicateAck", true);
            return;
        }

        redis.opsForValue().set(key, "1");

        exchange.setProperty("ack", ack);
    }
}