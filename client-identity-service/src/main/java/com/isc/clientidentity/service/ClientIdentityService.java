package com.isc.clientidentity.service;

import com.isc.clientidentity.api.ChallengeResponse;
import com.isc.clientidentity.api.RegisterClientRequest;
import com.isc.clientidentity.api.RegisterClientResponse;
import com.isc.clientidentity.api.VerifyProofRequest;
import com.isc.clientidentity.api.VerifyProofResponse;
import com.isc.clientidentity.config.ClientIdentityProperties;
import com.isc.clientidentity.model.ClientChallenge;
import com.isc.clientidentity.model.ClientIdentity;
import com.isc.clientidentity.model.ClientStatus;
import com.isc.clientidentity.repository.IdentityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class ClientIdentityService {
    private final IdentityRepository repository;
    private final ClientIdentityProperties properties;

    public ClientIdentityService(IdentityRepository repository, ClientIdentityProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public RegisterClientResponse register(RegisterClientRequest request) {
        byte[] encodedKey = decode(request.publicKey(), "public key");
        try {
            KeyFactory.getInstance(request.keyAlgorithm()).generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (Exception exception) {
            throw badRequest("publicKey is not a valid " + request.keyAlgorithm() + " key", exception);
        }
        Instant registeredAt = Instant.now();
        ClientIdentity identity = new ClientIdentity(request.cid(), request.did(), request.publicKey(),
                request.keyAlgorithm(), request.attestation(), ClientStatus.ACTIVE, registeredAt);
        repository.save(identity);
        return new RegisterClientResponse(identity.cid(), identity.did(), identity.status(), registeredAt);
    }

    public ChallengeResponse issueChallenge(String cid) {
        ClientIdentity identity = requireActive(cid);
        Instant expiresAt = Instant.now().plus(properties.getChallengeTtl());
        ClientChallenge challenge = new ClientChallenge(UUID.randomUUID().toString(), identity.cid(),
                Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes()), expiresAt);
        repository.saveChallenge(challenge);
        return new ChallengeResponse(challenge.id(), challenge.cid(), challenge.nonce(), challenge.expiresAt());
    }

    public VerifyProofResponse verify(VerifyProofRequest request) {
        ClientIdentity identity = requireActive(request.cid());
        if (!identity.did().equals(request.did())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "DID does not match the registered client");
        }
        ClientChallenge challenge = repository.consumeChallenge(request.challengeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Challenge is missing or expired"));
        if (!challenge.cid().equals(request.cid())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Challenge does not belong to the client");
        }
        boolean valid = verifySignature(identity, challenge.nonce(), request.signature());
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid proof-of-possession signature");
        }
        return new VerifyProofResponse(true, identity.cid(), identity.did(), identity.status().name(),
                "DEVICE_KEY_PROVEN");
    }

    public void revoke(String cid) {
        ClientIdentity identity = requireIdentity(cid);
        repository.save(new ClientIdentity(identity.cid(), identity.did(), identity.publicKey(), identity.keyAlgorithm(),
                identity.attestation(), ClientStatus.REVOKED, identity.registeredAt()));
    }

    private ClientIdentity requireActive(String cid) {
        ClientIdentity identity = requireIdentity(cid);
        if (identity.status() != ClientStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client identity is revoked");
        }
        return identity;
    }

    private ClientIdentity requireIdentity(String cid) {
        return repository.find(cid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client identity was not found"));
    }

    private boolean verifySignature(ClientIdentity identity, String nonce, String signature) {
        try {
            PublicKey publicKey = KeyFactory.getInstance(identity.keyAlgorithm())
                    .generatePublic(new X509EncodedKeySpec(decode(identity.publicKey(), "public key")));
            String algorithm = "Ed25519".equalsIgnoreCase(identity.keyAlgorithm())
                    ? "Ed25519" : "SHA256withECDSA";
            Signature verifier = Signature.getInstance(algorithm);
            verifier.initVerify(publicKey);
            verifier.update(nonce.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return verifier.verify(decode(signature, "signature"));
        } catch (Exception exception) {
            return false;
        }
    }

    private byte[] randomBytes() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private byte[] decode(String value, String name) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException exception) {
            throw badRequest(name + " must be standard Base64", exception);
        }
    }

    private ResponseStatusException badRequest(String message, Exception cause) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message, cause);
    }
}