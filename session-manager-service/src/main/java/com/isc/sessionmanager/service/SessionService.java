package com.isc.sessionmanager.service;

import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.contract.event.session.ClientDisconnectedEvent;

public interface SessionService {

    /**
     * Applies a connection lifecycle event (CONNECTED / DISCONNECTED / KEEPALIVE)
     * to the session store, creating or updating the Redis record as needed.
     *
     * Implementations must be idempotent and tolerant of out-of-order delivery
     * (e.g. a DISCONNECTED for an old sub-session arriving after a newer CONNECTED).
     */
    void handleConnectionEvent(ClientConnectedEvent event);
    void handleDisconnectionEvent(ClientDisconnectedEvent event);
}
