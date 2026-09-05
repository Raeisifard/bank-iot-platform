package com.isc.delivery.repository;

import com.isc.delivery.config.DeliveryProperties;
import com.isc.delivery.model.DeliveryRecord;
import com.isc.delivery.model.DeliveryState;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "delivery", name = {"enabled", "oracle.enabled"}, havingValue = "true")
public class OracleDeliveryStore {

    private final JdbcTemplate jdbcTemplate;
    private final DeliveryProperties properties;

    public boolean exists(String messageId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from " + table() + " where MESSAGE_ID = ?",
                Integer.class,
                messageId);
        return count != null && count > 0;
    }

    public void insertPending(DeliveryRecord record) {
        if (exists(record.messageId())) {
            return;
        }
        jdbcTemplate.update(
                "insert into " + table() + " " +
                        "(MESSAGE_ID, CLIENT_ID, PAYLOAD, DELIVERY_STATE, ATTEMPT_COUNT, " +
                        "CREATED_AT, UPDATED_AT, FAILURE_REASON) values (?, ?, ?, ?, ?, ?, ?, ?)",
                record.messageId(),
                record.clientId(),
                record.payload(),
                record.state().name(),
                record.attempt(),
                Timestamp.from(record.createdAt()),
                Timestamp.from(record.updatedAt()),
                record.failureReason());
    }

    public Optional<DeliveryRecord> find(String messageId) {
        List<DeliveryRecord> records = jdbcTemplate.query(
                "select MESSAGE_ID, CLIENT_ID, PAYLOAD, DELIVERY_STATE, ATTEMPT_COUNT, " +
                        "CREATED_AT, UPDATED_AT, FAILURE_REASON from " + table() +
                        " where MESSAGE_ID = ?",
                (rs, rowNum) -> new DeliveryRecord(
                        rs.getString("MESSAGE_ID"),
                        rs.getString("CLIENT_ID"),
                        rs.getString("PAYLOAD"),
                        rs.getInt("ATTEMPT_COUNT"),
                        DeliveryState.valueOf(rs.getString("DELIVERY_STATE")),
                        rs.getTimestamp("CREATED_AT").toInstant(),
                        rs.getTimestamp("UPDATED_AT").toInstant(),
                        rs.getString("FAILURE_REASON")),
                messageId);
        return records.stream().findFirst();
    }

    public void updateAttempt(String messageId, int attempt, Instant updatedAt) {
        jdbcTemplate.update(
                "update " + table() + " set ATTEMPT_COUNT = ?, UPDATED_AT = ? where MESSAGE_ID = ?",
                attempt,
                Timestamp.from(updatedAt),
                messageId);
    }

    public void markDelivered(String messageId, Instant updatedAt) {
        jdbcTemplate.update(
                "update " + table() + " set DELIVERY_STATE = ?, UPDATED_AT = ?, FAILURE_REASON = null " +
                        "where MESSAGE_ID = ?",
                DeliveryState.DELIVERED.name(),
                Timestamp.from(updatedAt),
                messageId);
    }

    public void markFailed(String messageId, DeliveryState state, String reason, Instant updatedAt) {
        jdbcTemplate.update(
                "update " + table() + " set DELIVERY_STATE = ?, UPDATED_AT = ?, FAILURE_REASON = ? " +
                        "where MESSAGE_ID = ?",
                state.name(),
                Timestamp.from(updatedAt),
                reason,
                messageId);
    }

    private String table() {
        String tableName = properties.getOracle().getTableName();
        if (tableName == null || !tableName.matches("[A-Za-z][A-Za-z0-9_$#]*")) {
            throw new IllegalStateException("Invalid delivery.oracle.table-name");
        }
        return tableName;
    }
}
