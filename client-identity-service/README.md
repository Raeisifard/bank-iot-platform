# Client Identity Service

Owns client installation identity and proof-of-possession. It is intentionally
separate from `token-service`: token issuance should consume this service's
successful verification result rather than implement device-key verification.

## Endpoints

`POST /api/v1/client-identities`

Registers a client CID, DID, X.509 SubjectPublicKeyInfo public key (standard
Base64), key algorithm (`EC` or `Ed25519`), and optional attestation payload.

`POST /api/v1/client-identities/{cid}/challenges`

Issues a short-lived, one-time nonce.

`POST /api/v1/client-identities/verify`

Consumes the challenge and verifies the signature over the nonce. A successful
response is the identity verification result that can be passed to
`token-service`.

`DELETE /api/v1/client-identities/{cid}`

Revokes the client identity.

Identity and challenge records use `common-redis` when
`common.redis.enabled=true`. Redis is disabled by default for local
development, where an in-process store is used instead.