# common-infrastructure

Shared Spring Boot auto-configuration for the bank-iot-platform.

Provides optional infrastructure beans for:

- Redis / Lettuce
- Kafka producer and consumer
- MongoDB
- Oracle / HikariCP / JdbcTemplate
- HashiCorp Vault / VaultTemplate

## Add dependency

```xml
<dependency>
    <groupId>com.isc</groupId>
    <artifactId>common-infrastructure</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Enable only what a service needs

Example acknowledge-service:

```yaml
infrastructure:
  redis:
    enabled: true
    host: ${REDIS_HOST}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:0}

  kafka:
    enabled: true
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    client-id: acknowledge-service
    consumer-group: acknowledge-service

  vault:
    enabled: true
    uri: ${VAULT_URI}
    token: ${VAULT_TOKEN}

  mongo:
    enabled: false

  oracle:
    enabled: false
```

Example delivery-service:

```yaml
infrastructure:
  redis:
    enabled: true
    host: ${REDIS_HOST}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}

  kafka:
    enabled: true
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    client-id: delivery-service
    consumer-group: delivery-service

  mongo:
    enabled: true
    uri: ${MONGO_URI}
    database: ${MONGO_DATABASE:banking_delivery}

  oracle:
    enabled: false

  vault:
    enabled: false
```

## Beans exposed

Redis:
- `RedisConnectionFactory`
- `StringRedisTemplate`

Kafka:
- `ProducerFactory<String,Object>`
- `KafkaTemplate<String,Object>`
- `ConsumerFactory<String,String>`
- `ConcurrentKafkaListenerContainerFactory<String,String>`

Mongo:
- `MongoClient`
- `MongoTemplate`

Oracle:
- `DataSource`
- `JdbcTemplate`

Vault:
- `VaultTemplate`

## Important

This module intentionally does not contain:
- business repositories
- Kafka topics
- Redis business keys
- Mongo collections
- Oracle SQL
- service-specific consumers
- transaction/delivery domain models

Those belong to individual services.

Production secrets should be supplied through environment variables, a deployment secret manager, or another external configuration mechanism. Do not commit passwords or Vault tokens to Git.
