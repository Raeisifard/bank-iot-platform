package com.isc.clientidentity.api;

import com.isc.clientidentity.model.ClientStatus;

import java.time.Instant;

public record RegisterClientResponse(String cid, String did, ClientStatus status, Instant registeredAt) {
}