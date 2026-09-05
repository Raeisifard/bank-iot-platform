# common-redis

Shared Redis infrastructure for `bank-iot-platform`.

## Responsibilities

This module owns:

- Redis connection configuration
- Redis connection creation
- `StringRedisTemplate`
- a small `RedisOperations` abstraction for key/value and hash operations

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

The first intended consumers are `token-service` and `session-manager-service`.

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
