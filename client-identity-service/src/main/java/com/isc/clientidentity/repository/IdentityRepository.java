package com.isc.clientidentity.repository;

import com.isc.clientidentity.model.ClientChallenge;
import com.isc.clientidentity.model.ClientIdentity;

import java.util.Optional;

public interface IdentityRepository {
    void save(ClientIdentity identity);
    Optional<ClientIdentity> find(String cid);
    void saveChallenge(ClientChallenge challenge);
    Optional<ClientChallenge> consumeChallenge(String challengeId);
}