package com.isc.acknowledge.service;

import com.isc.acknowledge.dto.AckRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AckService {

    private final StringRedisTemplate redis;

    public void handle(AckRequest ack) {

        String txKey =
                "tx:" + ack.getTransactionId();

        // -----------------------------------------
        // check transaction exists
        // -----------------------------------------

        if (!redis.hasKey(txKey)) {
            // optional: log orphan ACK
            return;
        }

        // -----------------------------------------
        // update state instead of delete (IMPORTANT)
        // -----------------------------------------

        redis.opsForValue().set(
                txKey,
                "ACKED:" + ack.getStatus()
        );

        // -----------------------------------------
        // optional cleanup of helper keys
        // -----------------------------------------

        redis.delete("tx:pending:" + ack.getTransactionId());
    }
}