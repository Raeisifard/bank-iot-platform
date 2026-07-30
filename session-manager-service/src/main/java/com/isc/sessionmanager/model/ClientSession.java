package com.isc.sessionmanager.model;

import com.isc.contract.event.session.ClientConnectedEvent;

import java.io.Serializable;

/**
 * The session record persisted in Redis (as a hash) under key
 * {@code session:<phoneNumber>}.
 *
 * status:
 *   ONLINE  - client currently connected to some EMQX node
 *   OFFLINE - client disconnected; record is kept briefly (or dropped,
 *             depending on downstream needs) before Redis TTL reaps it
 */
public record ClientSession(
        String SESSION_ID,
        String phoneNumber,
        String node,
        String protocol,
        SessionStatus status,
        long lastEventTimestamp,
        long updatedAt
) implements Serializable {

    public enum SessionStatus {
        ONLINE,
        OFFLINE
    }

    public static ClientSession connected(ClientConnectedEvent event, long now) {
        return new ClientSession(
                event.clientId(),
                event.phoneNumber(),
                event.node(),
                event.protocol(),
                SessionStatus.ONLINE,
                event.timestamp(),
                now
        );
    }

    public static ClientSession disconnected(ClientConnectedEvent event, long now) {
        return new ClientSession(
                event.clientId(),
                event.phoneNumber(),
                event.node(),
                event.protocol(),
                SessionStatus.OFFLINE,
                event.timestamp(),
                now
        );
    }
}
