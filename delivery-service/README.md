# delivery-service

`delivery-service` consumes messages from an inbound Kafka topic, records the
message in Oracle, publishes a delivery envelope to an outbound Kafka topic, and
tracks acknowledgement/retry state through `common-redis`.

## Workflow

1. Consume a record from `delivery.kafka.inbound-topic`.
2. Resolve `messageId` from the `messageId` Kafka header, record key, or a stable
   topic/partition/offset UUID.
3. Persist the payload and `PENDING` state in Oracle and Redis.
4. Publish a JSON `DeliveryEnvelope` to the outbound topic.
5. Schedule retries after 2 seconds and 5 seconds by default.
6. Consume `ack.event`; when its `messageId` matches, mark the Oracle row and Redis
   hash as `DELIVERED` and remove the retry entry.
7. After retry exhaustion, mark the durable row and Redis hash as `ARCHIVED`.

The outbound envelope contains `messageId`, `clientId`, `payload`,
`deliveryAttempt`, and `createdAt`. Downstream consumers should acknowledge using
that message ID through `acknowledge-service`.

## Redis audit state

Redis key `delivery:<messageId>` stores the message ID, client ID, state, attempt,
and update timestamp. State is one of `PENDING`, `DELIVERED`, `FAILED`, or
`ARCHIVED`. The retry schedule uses the `delivery:retry` sorted set. Delivered,
failed, archived, and pending TTLs are independently configurable.

Redis is accessed only through `common-redis`. Delivery processing is not started
unless `DELIVERY_ENABLED=true`, `DELIVERY_ORACLE_ENABLED=true`, and
`COMMON_REDIS_ENABLED=true`; disabled integrations do not create connections.

## Oracle

Apply [oracle-delivery-schema.sql](src/main/resources/db/oracle-delivery-schema.sql)
with the Oracle schema owner. The service does not run DDL automatically.

The Oracle table is the durable audit/outbox record. Redis is the operational retry
index and fast state lookup, not the system of record.

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `DELIVERY_ENABLED` | `false` | Enable delivery processing |
| `DELIVERY_ORACLE_ENABLED` | `false` | Enable Oracle persistence |
| `COMMON_REDIS_ENABLED` | `false` | Enable shared Redis |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `DELIVERY_INBOUND_TOPIC` | `delivery.inbound` | Input topic |
| `DELIVERY_OUTBOUND_TOPIC` | `mqtt.message.send` | Output topic |
| `DELIVERY_ACK_TOPIC` | `ack.event` | Acknowledgement topic |
| `ORACLE_JDBC_URL` | empty | Oracle JDBC URL |
| `ORACLE_USERNAME` / `ORACLE_PASSWORD` | empty | Oracle credentials |
| `DELIVERY_RETRY_DELAYS` | `2s,5s` | Retry delays |
| `DELIVERY_DELIVERED_TTL` | `30d` | Delivered Redis retention |
| `DELIVERY_FAILED_TTL` | `90d` | Failed Redis retention |
| `DELIVERY_ARCHIVED_TTL` | `180d` | Archived Redis retention |

All settings can be supplied by environment variables, mounted configuration,
Vault, or a future Spring Cloud Config Server. Secrets must not be committed.

## Run and test

```text
mvn spring-boot:run -pl delivery-service -am
mvn test -pl delivery-service -am
```

The service is a normal executable Spring Boot JAR and can run as a bare-metal
process or in Kubernetes. Configure health probes against the Actuator health
endpoint and provide Kafka, Oracle, and Redis through environment-specific
configuration.
