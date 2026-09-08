# Feature matrix

| Capability | Status | Notes |
|---|---|---|
| User identity | Implemented | Oracle system of record |
| OTP | Implemented | Redis challenge/hash/TTL/attempt limit |
| Biometric | Implemented as provider abstraction + dev mock | Production should verify cryptographic platform assertions |
| Device signature / TEE | Extension point + dev mock | Wire to client-identity-service/device attestation |
| Passkey/WebAuthn | Extension point | Provider can be added without changing transaction model |
| PIN/password | Model/policy extension point | Add credential storage using Argon2id/bcrypt policy before enabling |
| IAL-0..IAL-4 | Implemented | Policy-driven assurance ranking |
| Step-up | Implemented | Transaction remains pending until required level is reached |
| Replay protection | Implemented | One-time OTP and transaction lifecycle |
| Rate limiting | Implemented for OTP attempts/resend | Add gateway/distributed policy for production |
| Audit | Implemented | Oracle |
| Redis ephemeral state | Implemented | OTP and transaction state |
| Oracle durable state | Implemented | identity/authentication/audit |
| Token-service verification | Implemented | Internal endpoint; secure with mTLS/service auth in production |
| Risk engine | Extension point | Recommended before high-risk transactions |
| Transaction signing | Extension point | Recommended for high-value payments |
