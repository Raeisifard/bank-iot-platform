package com.isc.clientidentity.repository;

import com.isc.clientidentity.model.ClientChallenge;
import com.isc.clientidentity.model.ClientIdentity;
import com.isc.clientidentity.model.ClientStatus;
import com.isc.common.redis.RedisOperations;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RedisIdentityRepository implements IdentityRepository {
    private static final String CLIENT_PREFIX = "client-identity:";
    private static final String CHALLENGE_PREFIX = "client-identity:challenge:";

    private final RedisOperations redis;
    private final Map<String, ClientIdentity> localIdentities = new ConcurrentHashMap<>();
    private final Map<String, ClientChallenge> localChallenges = new ConcurrentHashMap<>();

    public RedisIdentityRepository(ObjectProvider<RedisOperations> redisProvider) {
        this.redis = redisProvider.getIfAvailable();
    }

    @Override
    public void save(ClientIdentity identity) {
        if (redis == null) {
            localIdentities.put(identity.cid(), identity);
            return;
        }
        redis.putAll(CLIENT_PREFIX + identity.cid(), Map.of(
                "cid", identity.cid(), "did", identity.did(), "publicKey", identity.publicKey(),
                "keyAlgorithm", identity.keyAlgorithm(),
                "attestation", identity.attestation() == null ? "" : identity.attestation(),
                "status", identity.status().name(), "registeredAt", identity.registeredAt().toString()));
    }

    @Override
    public Optional<ClientIdentity> find(String cid) {
        if (redis == null) {
            return Optional.ofNullable(localIdentities.get(cid));
        }
        Map<String, String> values = redis.entries(CLIENT_PREFIX + cid);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ClientIdentity(values.get("cid"), values.get("did"), values.get("publicKey"),
                values.get("keyAlgorithm"), values.get("attestation"), ClientStatus.valueOf(values.get("status")),
                Instant.parse(values.get("registeredAt"))));
    }

    @Override
    public void saveChallenge(ClientChallenge challenge) {
        if (redis == null) {
            localChallenges.put(challenge.id(), challenge);
            return;
        }
        String key = CHALLENGE_PREFIX + challenge.id();
        redis.putAll(key, Map.of("id", challenge.id(), "cid", challenge.cid(), "nonce", challenge.nonce(),
                "expiresAt", challenge.expiresAt().toString()));
        redis.expire(key, Duration.between(Instant.now(), challenge.expiresAt()));
    }

    @Override
    public Optional<ClientChallenge> consumeChallenge(String challengeId) {
        if (redis == null) {
            ClientChallenge challenge = localChallenges.remove(challengeId);
            return challenge == null || challenge.expiresAt().isBefore(Instant.now())
                    ? Optional.empty() : Optional.of(challenge);
        }
        String key = CHALLENGE_PREFIX + challengeId;
        Map<String, String> values = redis.entries(key);
        redis.delete(key);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        ClientChallenge challenge = new ClientChallenge(values.get("id"), values.get("cid"), values.get("nonce"),
                Instant.parse(values.get("expiresAt")));
        return challenge.expiresAt().isBefore(Instant.now()) ? Optional.empty() : Optional.of(challenge);
    }
}