# session-manager-service

Consumes client connection lifecycle events from Kafka (produced by the
Kafka Ingress Service from EMQX webhooks) and creates/updates the client's
session record in Redis.

## Where this fits

```
Flutter App --JWT--> EMQX --(validates via JWKS)--> Token Service
                        |
                connect/disconnect webhook
                        v
              Kafka Ingress Service --> Kafka topic: client-connection-events
                        |
                        v
              session-manager-service   <-- this module
                        |
                        v
                     Redis
```

## Message contract (Kafka topic: `client-connection-events`)

```json
{
  "clientId": "mqtt-client-abc123",
  "phoneNumber": "+989121234567",
  "eventType": "CONNECTED",
  "node": "emqx@10.0.1.5",
  "protocol": "mqtt",
  "timestamp": 1732600000000
}
```

`eventType` is one of `CONNECTED`, `DISCONNECTED`, `KEEPALIVE`.

If your Kafka Ingress Service's payload shape differs (e.g. different field
names, or EMQX's raw webhook body), adjust `ConnectionEvent` and/or add a
mapping step before it reaches `ConnectionEventListener`.

## Redis schema

| Key                        | Type | Value                                   | TTL                          |
|-----------------------------|------|------------------------------------------|------------------------------|
| `session:<phoneNumber>`     | JSON | `ClientSession` (clientId, node, status, timestamps) | `app.session.ttl-seconds` (default 180s, refreshed on every write) |

`phoneNumber` is the canonical key since EMQX resolves client identity from
phone number via bank transaction data. `clientId` is retained as an
attribute for debugging/reverse lookups.

Session TTL is a safety net — if a `DISCONNECTED` event is ever lost, the
key still expires on its own rather than reporting a client as online
forever. Set it comfortably above your EMQX keepalive interval.

## Idempotency / ordering

Every incoming event's `timestamp` is compared against the `lastEventTimestamp`
already stored in Redis. Events older than what's stored are discarded. This
protects against Kafka redelivery and out-of-order delivery across partitions/
consumer instances.

## Error handling

- Malformed / schema-invalid messages are routed straight to
  `client-connection-events.DLT` (no retry — they'll never succeed).
- Transient failures (e.g. Redis briefly unavailable) are retried with a
  fixed backoff (`app.kafka.retry.max-attempts`, `app.kafka.retry.backoff-ms`)
  before falling back to the DLT.
- Offsets are committed manually, only after a successful session write, so a
  crash mid-processing results in redelivery rather than data loss.

## Configuration

See `src/main/resources/application.yml`. Key environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka cluster |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `localhost` / `6379` / _empty_ | Redis connection |
| `CONNECTION_EVENTS_TOPIC` | `client-connection-events` | Source topic |
| `CONNECTION_EVENTS_DLT` | `client-connection-events.DLT` | Dead-letter topic |
| `SESSION_TTL_SECONDS` | `180` | Redis key TTL |

## Internal query API

`GET /internal/sessions/{phoneNumber}` — returns the current `ClientSession`
(404 if none/expired). Intended for other internal services (e.g. the
message-dispatch / acknowledge REST API) to check "is this phone number's
client currently online, and on which EMQX node" without touching Kafka or
Redis directly. Keep this endpoint off any internet-facing gateway.

## Integrating into your existing multi-module project

1. Copy this folder in as a sibling module (e.g. `modules/session-manager-service`).
2. Add it to your parent `pom.xml`'s `<modules>` list, or import as a standalone
   Spring Boot app if you deploy services independently.
3. Adjust the base package (`com.isc.sessionmanager`) to match your
   project's package convention.
4. Point `CONNECTION_EVENTS_TOPIC` at whatever topic your Kafka Ingress
   Service actually publishes to, and confirm the JSON field names match
   `ConnectionEvent`.
5. Create the dead-letter topic (`client-connection-events.DLT`) in your
   Kafka cluster, or let auto-topic-creation handle it in non-prod.

## Running tests

```
mvn test
```

Unit tests cover the core idempotency/session-transition logic in
`SessionServiceImplTest`. Add a Testcontainers-based integration test
(Kafka + Redis) if you want end-to-end coverage of the listener itself —
the `testcontainers` dependencies are already wired into `pom.xml` for this.
