# common-redis

Shared Redis infrastructure for `bank-iot-platform`.

## Responsibilities

This module owns:

- Redis connection configuration
- Redis connection creation
- `StringRedisTemplate`
- a small `RedisOperations` abstraction for key/value, hash, sorted-set, and Lua script operations

Service/domain modules should depend on this module instead of creating their own
`RedisConnectionFactory`, `LettuceConnectionFactory`, or `StringRedisTemplate`.

## Mandatory configuration

Every consuming service must explicitly configure:

```yaml
common:
  redis:
    enabled: true
```

or:

```yaml
common:
  redis:
    enabled: false
```

If `common.redis.enabled` is omitted, application startup fails.

When disabled, no Redis connection or `RedisOperations` bean is created.

## Connection configuration

Defaults can be overridden by the consuming service:

```yaml
common:
  redis:
    enabled: true
    url: redis://localhost:6379
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 2s
```

TLS is supported with `rediss://`.

## Integration

Add this directory as a module in the root Maven reactor:

```xml
<module>common-redis</module>
```

Then add the dependency to services that need Redis:

```xml
<dependency>
    <groupId>com.isc</groupId>
    <artifactId>common-redis</artifactId>
</dependency>
```

Current consumers include `acknowledge-service` and `session-manager-service`.
Future consumers such as `token-service` should also depend on this module rather
than adding a Redis starter or creating a Redis template directly.

## Consumer rules

Consumers inject `RedisOperations`; they must not inject Spring Data Redis types or
create connection factories/templates. The shared API keeps connection creation and
the `common.redis.enabled` switch in one place. Redis-dependent services should set
`COMMON_REDIS_ENABLED=false` for environments where Redis is intentionally absent;
the service may still require Redis for its business workflow, but it will not create
a Redis connection through this module while disabled.

Use `common.redis.url`, `common.redis.password`, `common.redis.database`, and
`common.redis.timeout` for local, bare-metal, container, Kubernetes, Vault, or future
Config Server overrides. Do not commit credentials or environment-specific hosts.

## Domain ownership

`common-redis` deliberately does not contain token/session business logic.

For example, `token-service` should implement its own:

```text
RefreshTokenStore
  -> RedisOperations
```

while `session-manager-service` continues to own session lifecycle and uses:

```text
SessionService
  -> RedisOperations
```

This keeps Redis infrastructure shared without coupling token and session domains.
