# This keys & values created in REDIS after TOKEN request in token-service module

| Name |
| :--- |
| refresh:d1ea4c85d21ea7c6a7acdb6481e6de36acb6263c09654fb0163109fe47d671ca |
| session:SID-c35d9c18-1be9-4983-9172-b7ec753a6679 |
| session:client:c426 |
| session:device:cust-1002:d425 |
| session:refresh:SID-c35d9c18-1be9-4983-9172-b7ec753a6679 |

---
This hash value comes from **RefreshTokenService** method in token (login flow) end-point and has absolute and dynamic ttls.

| KEY            | refresh:d1ea4c85d21ea7c6a7acdb6481e6de36acb6263c09654fb0163109fe47d671ca |
|:-----------------| :--- |
| createdAt        | 2026-08-02T06:01:16.988117200Z |
| clientId         | c426 |
| customerId       | cust-1002 |
| lastRotateAt     | 2026-08-02T06:01:16.988117200Z |
| sessionId        | SID-c35d9c18-1be9-4983-9172-b7ec753a6679 |
| refreshTokenHash | d1ea4c85d21ea7c6a7acdb6481e6de36acb6263c09654fb0163109fe47d671ca |
| expireAt         | 2026-09-11T06:01:16.988117200Z |
| deviceId         | d425 |
| status           | ACTIVE |
---
This hash value created by **token->login->RefreshTokenService.create()** and has ttl =  refresh-token-ttl: P30D and refresh-token-audit-ttl: P10D

| KEY            | session:SID-c35d9c18-1be9-4983-9172-b7ec753a6679 |
|:---------------| :--- |
| createdAt      | 2026-08-02T06:01:16.983118600Z |
| reason         | NONE |
| clientId       | c426 |
| refreshTokenId |  |
| customerId     | cust-1002 |
| sessionId      | SID-c35d9c18-1be9-4983-9172-b7ec753a6679 |
| expireAt       | 2026-10-31T06:01:16.983118600Z |
| deviceId       | d425 |
| lastRefreshAt  | 2026-08-02T06:01:16.983118600Z |
| status         | ONLINE |

And this is the updated one after client connected to EMQX:


| KEY            | session:SID-c35d9c18-1be9-4983-9172-b7ec753a6679 |
|:---------------| :--- |
| createdAt | 2026-08-02T06:01:16.983118600Z |
| reason | NONE |
| node | 123456-D |
| protocol | 5 |
| clientId | c426 |
| refreshTokenId |  |
| lastEventTimestamp | 1785663600076 |
| customerId | cust-1002 |
| ipAddress | 192.168.218.99:62833 |
| sessionId | SID-c35d9c18-1be9-4983-9172-b7ec753a6679 |
| expireAt | 2026-10-31T06:01:16.983118600Z |
| deviceId | d425 |
| lastRefreshAt | 2026-08-02T06:01:16.983118600Z |
| status | ONLINE |
| username | behnam |
| updatedAt | 2026-08-02T09:39:59.985018700Z |

---
This key/value set in token creation flow and is for finding SID by CID.

| KEY  | session:client:c426 |
|:--- |:---|
| VALUE | SID-c35d9c18-1be9-4983-9172-b7ec753a6679 |
---
This key/value set in token creation flow and is for finding SID by DID.

| KEY  | session:device:cust-1002:d425 |
|:--- |:---|
| VALUE | SID-c35d9c18-1be9-4983-9172-b7ec753a6679 |
---
This key/value set in **RefreshTokenService** and is for retrieving hashed-refresh-token by SID. 

| KEY  | session:refresh:SID-c35d9c18-1be9-4983-9172-b7ec753a6679 |
|:--- |:---|
| VALUE | d1ea4c85d21ea7c6a7acdb6481e6de36acb6263c09654fb0163109fe47d671ca |
