package com.isc.contract.event.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * JWT claims propagated inside connection lifecycle events.
 * {@code sid} is the primary key used to locate the session in Redis —
 * it replaces phone-number-based lookup entirely.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JwtInfo {
    private String sid; // session id -> Redis key: session:<sid>
    private String jti; // access/refresh token id
    private String iss; // token issuer
    private String did; // device id
    private String cid; // OAuth/app client id
    private String aud; // audience, e.g. ANDROID / IOS / WEB
}
