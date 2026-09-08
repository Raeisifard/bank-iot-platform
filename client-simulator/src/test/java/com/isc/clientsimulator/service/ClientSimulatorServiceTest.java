package com.isc.clientsimulator.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientSimulatorServiceTest {

    @Test
    void simulatorCanCreateIdentityProfile() {
        ClientIdentityProfile profile = new ClientIdentityProfile();
        profile.setCid("client-001");
        profile.setDid("did:key:example-001");
        profile.setSecurityLevel("TRUSTED_ENVIRONMENT");

        assertNotNull(profile.getCid());
        assertNotNull(profile.getDid());
        assertNotNull(profile.getSecurityLevel());
    }
}
