# common-vault

Shared Vault infrastructure module for the bank-iot-platform.

## Responsibility

This module is the only project module that should directly use Spring Vault APIs.

It provides:

- Vault connection/configuration
- KV v2 read/write
- Transit signing
- Transit public-key retrieval
- A small JWT key-store abstraction

## Configuration

The module ships with `application-common-vault.yml` containing development-safe defaults.

Enable it in an application with external configuration:

```yaml
vault:
  enabled: true
  uri: https://vault.example:8200
  token: ${VAULT_TOKEN}
  kv:
    enabled: true
    mount: key-management
  transit:
    enabled: true
    mount: transit
```

Production values should come from Config Server/environment, not from this JAR.

## Dependency rule

Other modules must not import:

- `VaultTemplate`
- `VaultEndpoint`
- `VaultVersionedKeyValueOperations`
- Spring Vault support classes

They should use the interfaces in this module instead.

# common-vault tests

This test folder contains both unit tests and real-Vault integration tests.

## Unit tests

These do not require a running Vault:

- `VaultKeyValueServiceTest`
- `VaultTransitServiceTest`

They mock `VaultTemplate` and verify the service behavior.

## Integration test

`VaultConnectionIT` requires a real Vault server.

It verifies:

1. Vault connectivity and authentication.
2. KV v2 mount and `bank-jwt` policy.
3. Required `JwtKeyPolicy` fields:
    - active
    - inactive
    - next
    - retired
4. Transit `bank-jwt` key.
5. Latest Transit version and public key.
6. Active policy version exists in Transit.
7. Every policy version referenced by active/inactive/next/retired exists in Transit.

## Environment variables

Set these before running the integration test:

    VAULT_TEST_URI
    VAULT_TEST_TOKEN
    VAULT_TEST_KV_MOUNT
    VAULT_TEST_TRANSIT_MOUNT
    VAULT_TEST_JWT_POLICY_PATH
    VAULT_TEST_TRANSIT_KEY_NAME

Defaults:

    VAULT_TEST_URI=http://localhost:8200
    VAULT_TEST_KV_MOUNT=key-management
    VAULT_TEST_TRANSIT_MOUNT=transit
    VAULT_TEST_JWT_POLICY_PATH=bank-jwt
    VAULT_TEST_TRANSIT_KEY_NAME=bank-jwt

Never commit a real Vault token to source control.

## Maven

The integration test intentionally uses the `IT` suffix. If your parent POM runs
only `*Test` during the normal Maven test phase, run this integration test
explicitly or configure Maven Failsafe in the parent project.
