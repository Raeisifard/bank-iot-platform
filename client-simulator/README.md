# client-simulator

## Purpose

`client-simulator` is a Spring Boot-based developer and integration-testing laboratory for the Bank IoT Platform. It models the future Flutter/Android mobile client in a way that is useful for exercising the existing platform contracts without requiring a real Android device.

This module is intentionally a simulation engine, not a replacement for a real Android application. It simulates the protocol flow, security model, attestation metadata, MQTT behavior, and REST interactions that the real platform expects from a mobile client.

## Module ownership

This module is part of the platform's developer tooling and integration testing layer. It interacts with:

- `client-identity-service`
- `token-service`
- MQTT brokers such as EMQX
- the platform HTTP APIs

It must remain configurable and startable without requiring Oracle, MongoDB, Kafka, Redis, or a real Android environment to be available.

## Core responsibilities

- Generate simulated client identities
- Model device security profiles such as SOFTWARE, TRUSTED_ENVIRONMENT and STRONGBOX
- Generate simulated Android attestation payloads and metadata
- Generate DID/public-key artifacts consistent with the identity service contract
- Exercise challenge/response proof-of-possession flows
- Connect to the real REST APIs when configured
- Provide a lightweight browser UI for manual testing
- Support deterministic and random test scenarios

## Supported workflow

The simulator is designed to support the following flow:

1. Generate a simulated client key
2. Generate a DID from the public key metadata
3. Generate a simulated Android attestation record
4. Register the client with `client-identity-service`
5. Request a challenge
6. Sign the nonce using the simulated key
7. Submit proof-of-possession
8. Request a token from `token-service`
9. Connect MQTT and exercise message flows
10. Run scenario-driven testing

## Default ports

- application: `http://localhost:8090`
- identity service: `http://localhost:8085`
- token service: `http://localhost:8083`
- MQTT TCP: `localhost:1883`

## Configuration

The module can be configured through `application.yml` and environment variables.

```yaml
server:
  port: ${SERVER_PORT:8090}

simulator:
  client-identity-service:
    base-url: ${CLIENT_IDENTITY_SERVICE_URL:http://localhost:8085}
  token-service:
    base-url: ${TOKEN_SERVICE_URL:http://localhost:8083}
  mqtt:
    host: ${MQTT_HOST:localhost}
    tcp-port: ${MQTT_TCP_PORT:1883}
    tls-port: ${MQTT_TLS_PORT:8883}
    websocket-port: ${MQTT_WS_PORT:8084}
  android:
    package-name: ${SIMULATOR_PACKAGE_NAME:com.bank.mobile}
    version: ${SIMULATOR_APP_VERSION:1.0.0}
  security:
    default-level: ${SIMULATOR_SECURITY_LEVEL:TRUSTED_ENVIRONMENT}
```

## Browser UI

The application exposes a simple static UI at:

```text
http://localhost:8090/
```

It includes controls for:

- generate identity
- generate attestation
- enroll client
- request challenge
- verify proof
- request access token

## Security disclaimer

This simulator explicitly models Android-style security concepts as a protocol and integration test aid. It uses labels such as:

- `SIMULATED TEE`
- `SIMULATED STRONGBOX`
- `SIMULATED ANDROID KEYSTORE`
- `SIMULATED KEY ATTESTATION`

It must not be interpreted as proving a real hardware-backed Android security boundary exists on the host machine.

## Local development commands

From the repository root:

```bash
mvn -pl client-simulator -am test
mvn -pl client-simulator spring-boot:run
```

## API examples

### Generate simulated identity

```bash
curl -X POST http://localhost:8090/api/v1/simulator/identity/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "cid": "client-001",
    "did": "did:key:example-001",
    "securityLevel": "TRUSTED_ENVIRONMENT"
  }'
```

### Generate simulated attestation

```bash
curl -X POST http://localhost:8090/api/v1/simulator/attestation/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "cid": "client-001",
    "did": "did:key:example-001",
    "securityLevel": "STRONGBOX"
  }'
```

### Request challenge

```bash
curl "http://localhost:8090/api/v1/simulator/challenge?cid=client-001"
```

## Notes

- This module intentionally remains independent from Oracle, MongoDB, Redis, and Kafka unless those services are explicitly configured.
- The UI is a console for the simulation engine; it is not the authoritative source of state.
- Production-specific behavior should remain in the platform services, while this module focuses on modeled client behavior and testing loops.
