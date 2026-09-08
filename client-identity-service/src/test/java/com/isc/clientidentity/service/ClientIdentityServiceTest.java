package com.isc.clientidentity.service;

import com.isc.clientidentity.api.ChallengeResponse;
import com.isc.clientidentity.api.RegisterClientRequest;
import com.isc.clientidentity.api.VerifyProofRequest;
import com.isc.clientidentity.config.ClientIdentityProperties;
import com.isc.clientidentity.model.ClientChallenge;
import com.isc.clientidentity.model.ClientIdentity;
import com.isc.clientidentity.repository.IdentityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientIdentityServiceTest {
    @Test
    void verifiesProofAndRejectsReplay() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(256);
        KeyPair keyPair = generator.generateKeyPair();
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository();
        ClientIdentityProperties properties = new ClientIdentityProperties();
        properties.setChallengeTtl(Duration.ofMinutes(2));
        ClientIdentityService service = new ClientIdentityService(repository, properties);

        service.register(new RegisterClientRequest("cid-1", "did:key:1",
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()), "EC", null));
        ChallengeResponse challenge = service.issueChallenge("cid-1");

        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(keyPair.getPrivate());
        signer.update(challenge.nonce().getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signer.sign());

        var verification = service.verify(
            new VerifyProofRequest("cid-1", "did:key:1", challenge.challengeId(), signature));
        assertTrue(verification.valid());
        assertEquals("DEVICE_KEY_PROVEN", verification.identityLevel());
        assertThrows(ResponseStatusException.class,
                () -> service.verify(new VerifyProofRequest("cid-1", "did:key:1", challenge.challengeId(), signature)));
    }

    private static final class InMemoryIdentityRepository implements IdentityRepository {
        private final Map<String, ClientIdentity> identities = new ConcurrentHashMap<>();
        private final Map<String, ClientChallenge> challenges = new ConcurrentHashMap<>();

        @Override
        public void save(ClientIdentity identity) {
            identities.put(identity.cid(), identity);
        }

        @Override
        public Optional<ClientIdentity> find(String cid) {
            return Optional.ofNullable(identities.get(cid));
        }

        @Override
        public void saveChallenge(ClientChallenge challenge) {
            challenges.put(challenge.id(), challenge);
        }

        @Override
        public Optional<ClientChallenge> consumeChallenge(String challengeId) {
            return Optional.ofNullable(challenges.remove(challengeId));
        }
    }
}