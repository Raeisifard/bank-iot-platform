# Client Identity Service

Owns client installation identity and proof-of-possession. It is intentionally
separate from `token-service`: token issuance should consume this service's
successful verification result rather than implement device-key verification.

## Endpoints

The service listens on `http://localhost:8085` by default. Set `SERVER_PORT`
to use a different port.

### Register a client

`POST /api/v1/client-identities`

Registers a client installation. `publicKey` must be a standard Base64-encoded
X.509 SubjectPublicKeyInfo public key. The supported key algorithms are `EC`
and `Ed25519`. The `attestation` field is optional.

```bash
curl -i -X POST http://localhost:8085/api/v1/client-identities \
	-H "Content-Type: application/json" \
	-d '{
		"cid": "client-001",
		"did": "did:key:example-001",
		"publicKey": "<STANDARD_BASE64_X509_PUBLIC_KEY>",
		"keyAlgorithm": "EC",
		"attestation": "optional-attestation-data"
	}'
```

Required fields are `cid`, `did`, `publicKey`, and `keyAlgorithm`.
Successful requests return `201 Created`:

```json
{
	"cid": "client-001",
	"did": "did:key:example-001",
	"status": "ACTIVE",
	"registeredAt": "2026-09-08T12:00:00Z"
}
```

### Issue a challenge

`POST /api/v1/client-identities/{cid}/challenges`

Issues a short-lived, one-time nonce for proof-of-possession. The default
challenge lifetime is two minutes.

```bash
curl -i -X POST \
	http://localhost:8085/api/v1/client-identities/client-001/challenges
```

The response contains the `challengeId` and the `nonce`. The client must sign
the nonce with its private key:

```json
{
	"challengeId": "f8b7c6d5-e4f3-4a21-9876-123456789abc",
	"cid": "client-001",
	"nonce": "random-base64url-nonce",
	"expiresAt": "2026-09-08T12:02:00Z"
}
```

### Verify client proof

`POST /api/v1/client-identities/verify`

Consumes the challenge and verifies the signature over the returned nonce. The
signature must be standard Base64-encoded. For `EC`, the service uses
`SHA256withECDSA`; for `Ed25519`, it uses `Ed25519`.

```bash
curl -i -X POST http://localhost:8085/api/v1/client-identities/verify \
	-H "Content-Type: application/json" \
	-d '{
		"cid": "client-001",
		"did": "did:key:example-001",
		"challengeId": "f8b7c6d5-e4f3-4a21-9876-123456789abc",
		"signature": "<STANDARD_BASE64_SIGNATURE_OF_NONCE>"
	}'
```

Successful verification returns an identity result that can be passed to
`token-service`:

```json
{
	"valid": true,
	"cid": "client-001",
	"did": "did:key:example-001",
	"clientStatus": "ACTIVE",
	"identityLevel": "DEVICE_KEY_PROVEN"
}
```

The challenge is consumed after verification and cannot be replayed.

For example, an EC client can generate a key and sign the returned nonce with
OpenSSL:

```bash
openssl ecparam -name prime256v1 -genkey -noout -out client-key.pem

openssl pkey -in client-key.pem -pubout -outform DER | base64 -w0

NONCE="random-base64url-nonce"
SIGNATURE=$(printf '%s' "$NONCE" | \
	openssl dgst -sha256 -sign client-key.pem -binary | base64 -w0)
```

Use the first command's public-key output as `publicKey` during registration
and `$SIGNATURE` as the `signature` in the verification request.

### Revoke a client

`DELETE /api/v1/client-identities/{cid}`

Marks the client identity as revoked. Revoked clients cannot receive new
challenges or successfully verify proofs.

```bash
curl -i -X DELETE \
	http://localhost:8085/api/v1/client-identities/client-001
```

Successful requests return `204 No Content`.

## Typical call sequence

1. Register the client and its public key.
2. Request a challenge for the client.
3. Sign the returned nonce with the client's private key.
4. Submit the signature for verification.
5. Use the successful verification result when requesting a token.

## Health endpoint

Spring Boot Actuator exposes the service health endpoint:

```bash
curl http://localhost:8085/actuator/health
```

Identity and challenge records use `common-redis` when
`common.redis.enabled=true`. Redis is disabled by default for local
development, where an in-process store is used instead.