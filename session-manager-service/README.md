# session-manager-service

Consumes client connection lifecycle events from Kafka (produced by
`mqtt-ingress-service` from EMQX webhooks) and creates/updates the client's
session record in Redis. Also exposes a gRPC service so `token-service` can
manage the same session records at authentication time.

## Where this fits

```
Flutter App --JWT--> EMQX --(validates via JWKS)--> Token Service
                        |
                connect/disconnect webhook
                        v
              mqtt-ingress-service --> Kafka topic: mqtt.client.connection
                        |
                        v
              session-manager-service   <-- this module
                        |
                        v
                     Redis
```

## Message contract (Kafka topic: `mqtt.client.connection`)

`mqtt-ingress-service` publishes two distinct event classes to the **same**
topic — `com.isc.contract.event.session.ClientConnectedEvent` and
`com.isc.contract.event.session.ClientDisconnectedEvent` (both extend
`BaseKafkaEvent`, which carries `eventType`: `CLIENT_CONNECTED`,
`CLIENT_DISCONNECTED`, or `CLIENT_KEEPALIVE` for connected-shaped keepalive
records). Spring's default `JsonSerializer` stamps a `__TypeId__` header with
each record's fully-qualified class name, and `ConnectionEventListener`
dispatches on that Java type — `KafkaConsumerConfig` must have
`JsonDeserializer.USE_TYPE_INFO_HEADERS=true` for this to work; forcing a
single default type here will silently mis-decode disconnect events.

```json
{
  "eventType": "CLIENT_CONNECTED",
  "clientId": "CUST-1002",
  "username": "behnam",
  "ipAddress": "192.168.1.1",
  "protocol": 5,
  "nodeId": "emqx@10.0.1.5",
  "connectedAt": 1732600000000,
  "jwt": { "sid": "SID-123", "did": "DEVICE-1", "cid": "MOBILE-APP" }
}
```

Session identity is `jwt.sid`, not `clientId` (`clientId` is the MQTT client
id / EMQX identity, kept only as an attribute for debugging).

## Redis schema

Sessions are stored as a **Redis hash** per `sessionId` (= `jwt.sid`), not as
a JSON blob keyed by phone number:

| Key                              | Type | Fields                                                              | TTL |
|-----------------------------------|------|----------------------------------------------------------------------|-----|
| `session:<sessionId>`             | Hash | sessionId, customerId, deviceId, clientId, username, ipAddress, node, protocol, status, lastEventTimestamp, createdAt, updatedAt, ... | `app.session.ttl-seconds` (default 180s), refreshed on every CONNECTED/KEEPALIVE |
| `session:device:<customerId>:<deviceId>` | String | sessionId | same TTL, set at auth time |
| `session:client:<clientId>`       | String | sessionId | same TTL, set at auth time |

`SessionServiceImpl` is the single owner of this schema and is shared by both
the Kafka listener (connectivity updates) and the gRPC service (auth-time
create/validate/revoke, used by `token-service`).

## Idempotency / ordering

Every incoming event's timestamp (`connectedAt` / `disconnectedAt`) is
compared against `lastEventTimestamp` already stored in Redis. Events older
than what's stored are discarded. This protects against Kafka redelivery and
out-of-order delivery across partitions/consumer instances. A
`CLIENT_DISCONNECTED` event for a session that doesn't exist in Redis is
ignored outright — a disconnect must never create a session.

## Error handling

- Malformed / schema-invalid messages are routed straight to
  `client-connection-events.DLT` (no retry — they'll never succeed).
- Transient failures (e.g. Redis briefly unavailable) are retried with a
  fixed backoff (`app.kafka.retry.max-attempts`, `app.kafka.retry.backoff-ms`)
  before falling back to the DLT.
- Offsets are committed manually, only after a successful session write, so a
  crash mid-processing results in redelivery rather than data loss.

## Configuration

See `src/main/resources/application.yml`. Redis is provided exclusively by the
`common-redis` module through `RedisOperations`; this service does not create a
`RedisTemplate` or configure `spring.data.redis` directly.

Key environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka cluster |
| `COMMON_REDIS_ENABLED` | `false` | Enable the shared Redis connection |
| `COMMON_REDIS_URL` / `REDIS_PASSWORD` / `REDIS_DATABASE` / `REDIS_TIMEOUT` | see application.yml | Shared Redis connection |
| `CONNECTION_EVENTS_TOPIC` | `client-connection-events` | Source topic (see `app.kafka.topic.connection-events`) |
| `CONNECTION_EVENTS_DLT` | `client-connection-events.DLT` | Dead-letter topic |
| `SESSION_TTL_SECONDS` | `180` | Redis key TTL, safety net re-applied on every CONNECTED/KEEPALIVE |

`KafkaConsumerConfig` defines its own `ConsumerFactory`/container factory
beans used by `ConnectionEventListener`; the `spring.kafka.consumer.*` block
in `application.yml` is not authoritative for that listener but is kept
aligned with it for anyone reading the config.

## Internal query API

`GET /internal/sessions/{sessionId}` — returns the current session (404 if
none/expired). Intended for other internal services (e.g. the
message-dispatch / acknowledge REST API) to check whether a given session is
currently online, and on which EMQX node, without touching Kafka or Redis
directly. Keep this endpoint off any internet-facing gateway.

If a caller only has a phone number, resolving it to a `sessionId` is a
`token-service` concern (that's where the phone → session mapping is owned),
not something this endpoint does.

## Integrating into your existing multi-module project

1. Copy this folder in as a sibling module (e.g. `modules/session-manager-service`).
2. Add it to your parent `pom.xml`'s `<modules>` list, or import as a standalone
   Spring Boot app if you deploy services independently.
3. Adjust the base package (`com.isc.sessionmanager`) to match your
   project's package convention — and update `JsonDeserializer.TRUSTED_PACKAGES`
   in `KafkaConsumerConfig` to match wherever `ClientConnectedEvent`/
   `ClientDisconnectedEvent` actually live in your build.
4. Configure `COMMON_REDIS_ENABLED=true` and the `COMMON_REDIS_*`/`REDIS_*`
   connection settings when Redis is available. Keep it false for environments
   where this Redis-dependent service is not being started.
5. Point `CONNECTION_EVENTS_TOPIC` at whatever topic your ingress service
   actually publishes to, and confirm the JSON field names match the event
   classes in `common-contract-kafka`.
6. Create the dead-letter topic (`client-connection-events.DLT`) in your
   Kafka cluster, or let auto-topic-creation handle it in non-prod.

## Running tests

```
mvn test
```

`SessionServiceImplTest` covers the idempotency/stale-event/connectivity
logic. Add a Testcontainers-based integration test (Kafka + Redis) if you
want end-to-end coverage of the listener itself — the `testcontainers`
dependencies are already wired into `pom.xml` for this.
