# token-service

## Purpose

`token-service` is the dedicated token-issuing service. Its responsibility is to issue all supported token types and expose the HTTP endpoints and JWKS functionality required by token consumers.

**It has nothing to do with `session-manager-service`.** Session creation, session state, refresh/session lifecycle, revocation state, device sessions, and session expiration belong to the session-management domain and are intentionally outside this module.

## Architecture

```text
                         +----------------------+
                         |      Client/App      |
                         +----------+-----------+
                                    |
                                    | HTTP
                                    v
                    +-----------------------------+
                    |        token-service        |
                    |                             |
                    | Token endpoints             |
                    | Access/refresh/etc. tokens  |
                    | JWT claims & lifetimes      |
                    | Signing-key policy          |
                    | Key rotation orchestration  |
                    | JWKS endpoint               |
                    +-------------+---------------+
                                  |
                    +-------------+---------------+
                    |                             |
                    v                             v
             +-------------+               +-------------+
             | common-web  |               |common-      |
             | HTTP support|               |security     |
             +-------------+               |security API |
                                           +------+------+
                                                  |
                                                  v
                                           +-------------+
                                           | common-vault|
                                           | Vault access|
                                           | KV / Transit|
                                           +------+------+
                                                  |
                                                  v
                                           +-------------+
                                           | HashiCorp   |
                                           | Vault       |
                                           +-------------+

   Separate responsibility:
   +-----------------------------------------------+
   | session-manager-service                       |
   | Session lifecycle/state/revocation/expiry     |
   +-----------------------------------------------+
```

## Module relationships

```text
token-service
   |
   +--> common-web
   |
   +--> common-security
           |
           +--> common-vault
```

The dependency direction is intentional. `token-service` must not contain a second Vault implementation and should not directly use Spring Vault APIs.

## Core responsibilities

### Token issuance
`AuthController` exposes the token endpoint and delegates to `TokenService`.

### JWT generation
`JwtGeneratorService` builds the token and is the boundary where production cryptographic signing is connected to the Vault-backed signing capability.

### Key management
The `keymanagement` package owns token-service's signing-key policy, versions, status and rotation orchestration. Policy is not Vault infrastructure.

### JWKS
`JwksController` exposes:

`GET /.well-known/jwks.json`

`JwksService` obtains publishable key versions and converts them to JWK representations.

### Key rotation
A new key becomes active while the previous public key can remain published during an overlap period. This allows consumers to validate tokens issued before rotation.

```text
V1 ACTIVE
   |
   | rotate
   v
V2 ACTIVE + V1 PUBLISHED
   |
   | overlap/token lifetime
   v
V2 ACTIVE + V1 RETIRED
```

## What does NOT belong here

Do not put these responsibilities into token-service:

- session registry
- session-manager-service client
- device-session lifecycle
- session expiration/revocation state
- MQTT connection state
- business transaction state
- direct Vault client implementation
- JWT validation infrastructure intended for other services

A token may contain a `jti`, subject, client ID, or other claims, but that does not make token-service the owner of session lifecycle.

## common-security relationship

`common-security` is primarily a reusable consumer-side security library:

- JWT validation
- authentication
- authorization
- public-key resolution
- public-key cache
- security-facing Vault abstractions

`token-service` is the producer-side token service:

- generate JWTs
- select signing key
- apply token policy
- rotate signing keys
- publish JWKS

Therefore:

```text
token-service      -> generates tokens
common-security    -> validates tokens
common-vault       -> performs Vault infrastructure operations
```

## common-vault relationship

All Vault infrastructure remains centralized in `common-vault`.

The intended path is:

```text
token-service
     |
     v
security/Vault abstraction
     |
     v
common-vault
     |
     +--> KV
     +--> Transit
     |
     v
Vault
```

Token-service must not instantiate `VaultTemplate` or contain Vault credentials.

## Package structure

```text
com.isc.token
├── TokenServiceApplication.java
├── controller
│   ├── AuthController.java
│   └── JwksController.java
├── service
│   ├── TokenService.java
│   ├── TokenServiceImpl.java
│   ├── JwtGeneratorService.java
│   └── JwtGeneratorServiceImpl.java
├── keymanagement
│   ├── JwtKeyPolicy.java
│   ├── JwtKeyPolicyService.java
│   ├── JwtKeyPolicyServiceImpl.java
│   ├── JwtKeyRotationService.java
│   ├── JwtKeyRotationServiceImpl.java
│   ├── JwtKeyVersion.java
│   ├── JwtKeyStatus.java
│   └── JwtKeyMetadata.java
├── jwks
│   ├── JwksService.java
│   ├── JwksServiceImpl.java
│   ├── JwkProvider.java
│   └── JwkProviderImpl.java
├── dto
│   ├── TokenRequest.java
│   ├── TokenResponse.java
│   └── JwksResponse.java
├── config
│   ├── JwtProperties.java
│   ├── TokenServiceProperties.java
│   └── TokenServiceConfiguration.java
├── model
│   ├── TokenClaims.java
│   └── SigningKey.java
└── exception
    ├── TokenServiceException.java
    ├── TokenGenerationException.java
    ├── KeyRotationException.java
    └── InvalidTokenRequestException.java
```

## Configuration

Use normal Spring Boot `application.yml` / profile-specific files. Do not invent a custom `token-service-defaults.yml` that Spring Boot will not automatically load.

Example:

```yaml
token:
  service:
    enabled: true
    issuer: isc-token-service
    audience: isc-services
    clock-skew: 30s
    access-token-lifetime: 15m
    refresh-token-lifetime: 30d

  jwt:
    algorithm: RS256
    key-id-prefix: jwt
    access-token-lifetime: 15m
    refresh-token-lifetime: 30d
```

Infrastructure credentials should be supplied externally through environment/config-server configuration.

## Endpoint flow

```text
POST /api/v1/auth/token
        |
        v
AuthController
        |
        v
TokenService
        |
        v
JwtGeneratorService
        |
        +--> claims
        +--> lifetime
        +--> active signing key
        +--> cryptographic signing
        |
        v
TokenResponse
```

JWKS:

```text
GET /.well-known/jwks.json
        |
        v
JwksController
        |
        v
JwksService
        |
        v
JwtKeyPolicyService
        |
        v
JwkProvider
        |
        v
JwksResponse
```

## Testing

The test structure covers token issuance, controller behavior, key policy/version behavior and JWKS generation.

Production signing tests should additionally verify the real Vault Transit/common-vault integration once the exact common-vault API is wired into this module.

## Important implementation note

This ZIP is an architectural implementation/scaffold, not a claim that the placeholder token strings are production JWTs. The production signing adapter must be connected to the exact `common-vault` APIs from the previously established module and must produce real signed JWTs with the required claims and key IDs.

The deliberate boundary is:

**token-service owns token issuance and related endpoints; session-manager-service owns sessions; common-security validates tokens; common-vault owns Vault infrastructure.**
