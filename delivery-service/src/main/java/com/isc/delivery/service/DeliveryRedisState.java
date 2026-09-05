package com.isc.delivery.service;

import com.isc.common.constants.RedisKeys;
import com.isc.common.redis.RedisOperations;
import com.isc.delivery.config.DeliveryProperties;
import com.isc.delivery.model.DeliveryRecord;
import com.isc.delivery.model.DeliveryState;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "delivery", name = {"enabled", "oracle.enabled"}, havingValue = "true")
@ConditionalOnProperty(prefix = "common.redis", name = "enabled", havingValue = "true")
public class DeliveryRedisState {

    private static final String STATE = "state";
    private static final String MESSAGE_ID = "messageId";
    private static final String CLIENT_ID = "clientId";
    private static final String ATTEMPT = "attempt";
    private static final String UPDATED_AT = "updatedAt";
    private static final String RETRY_KEY = "delivery:retry";

    private final RedisOperations redis;
    private final DeliveryProperties properties;

    public void pending(DeliveryRecord record) {
        write(record, DeliveryState.PENDING, properties.getAudit().getPendingTtl());
    }

    public void scheduleRetry(String messageId, Instant nextAttemptAt) {
        redis.addToSortedSet(RETRY_KEY, messageId, nextAttemptAt.toEpochMilli());
    }

    public Set<String> dueRetries(Instant now) {
        return redis.rangeFromSortedSetByScore(RETRY_KEY, 0, now.toEpochMilli());
    }

    public void removeRetry(String messageId) {
        redis.removeFromSortedSet(RETRY_KEY, messageId);
    }

    public Optional<DeliveryState> state(String messageId) {
        return redis.get(RedisKeys.MESSAGE_DELIVERY + messageId, STATE).map(DeliveryState::valueOf);
    }

    public void delivered(DeliveryRecord record) {
        removeRetry(record.messageId());
        write(record, DeliveryState.DELIVERED, properties.getAudit().getDeliveredTtl());
    }

    public void failed(DeliveryRecord record, DeliveryState state, Duration ttl) {
        removeRetry(record.messageId());
        write(record, state, ttl);
    }

    private void write(DeliveryRecord record, DeliveryState state, Duration ttl) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(MESSAGE_ID, record.messageId());
        values.put(CLIENT_ID, record.clientId());
        values.put(STATE, state.name());
        values.put(ATTEMPT, String.valueOf(record.attempt()));
        values.put(UPDATED_AT, Instant.now().toString());
        redis.putAll(RedisKeys.MESSAGE_DELIVERY + record.messageId(), values);
        redis.expire(RedisKeys.MESSAGE_DELIVERY + record.messageId(), ttl);
    }
}
