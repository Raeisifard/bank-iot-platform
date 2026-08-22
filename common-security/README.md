# common-security

JWT validation and authentication abstractions for the bank-iot-platform.

## Boundary

`common-security -> common-vault -> Vault`

`common-security` never injects `VaultTemplate`, never reads Vault directly, and never contains Vault credentials. It uses the public `VaultJwtKeyStore` and `VaultKeyValueService` contracts from `common-vault`.

## JWT validation

1. Parse JWT and require `kid`.
2. Resolve `kid` through the JWT key policy stored in Vault KV.
3. Resolve the policy's `vkv` (Vault Transit key version).
4. Read the corresponding public key through `VaultJwtKeyStore`.
5. Cache the parsed RSA public key by JWT `kid` for 15 minutes.
6. Verify RS256 (or configured algorithm) signature.
7. Validate `exp`, `nbf`, `iat`, issuer and configured audience.
8. Expose validated claims and a `SecurityPrincipal`.
9. `validateAndExtractPrincipal()` is provided for `common-web`.

The JWT key policy is owned by `token-service`; common-security only consumes it. This supports external JWT `kid` values such as `bank-260815...` that are different from Vault Transit integer versions.

## Configuration

```yaml
common:
  security:
    jwt:
      enabled: true
      algorithm: RS256
      clock-skew: 30s
      vault-key-name: bank-jwt-v2
      policy-path: jwt/key-policy
      issuer:
      audiences: []
    cache:
      public-keys:
        enabled: true
        ttl: 15m
        maximum-size: 100
```

For services that must expose HTTP endpoints but must not authenticate requests (for example token-service's token issuance endpoint), keep `common-web.authentication-enabled=false`. The validator bean is still only created when the required common-vault services exist.

## Important

The cache TTL is independent of Vault key rotation. A rotated key does not change the TTL of already cached keys. `common-web` owns HTTP filter wiring; `token-service` owns token issuance, key rotation and JWKS composition.
