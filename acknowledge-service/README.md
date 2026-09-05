# acknowledge-service

Consumes acknowledgement events and updates message delivery state in Redis.
Redis infrastructure is provided exclusively by `common-redis`; this module uses
the shared `RedisOperations` abstraction for hashes, retry sorted sets, and the
atomic acknowledgement Lua script.

## Configuration

Configure `src/main/resources/application.yml` with environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `COMMON_REDIS_ENABLED` | `false` | Enable the shared Redis connection |
| `COMMON_REDIS_URL` | `redis://localhost:6379` | Redis URL, including `rediss://` for TLS |
| `REDIS_PASSWORD` | empty | Redis password |
| `REDIS_DATABASE` | `0` | Redis database number |
| `REDIS_TIMEOUT` | `2s` | Redis command timeout |
| `KAFKA_BOOTSTRAP_SERVERS` | repository default | Kafka bootstrap servers |

The service does not declare a Redis starter or create a Redis template. The
`common.redis.*` settings are suitable for local, bare-metal, container, and
Kubernetes deployment and can later be supplied by Spring Cloud Config or Vault.

## Running

```text
mvn spring-boot:run -pl acknowledge-service -am
```

For a functional run, enable Redis and Kafka through environment variables. Do
not commit credentials or production endpoints. Health and operational endpoints
follow the application dependencies and deployment configuration.

## Testing

```text
mvn test -pl acknowledge-service -am
```