package com.isc.clientsimulator.controller;

import com.isc.clientsimulator.service.ClientIdentityProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/simulator")
public class SimulatorController {

    @PostMapping("/identity/generate")
    public ResponseEntity<Map<String, Object>> generateIdentity(@RequestBody Map<String, String> payload) {
        String cid = payload.getOrDefault("cid", "client-001");
        String did = payload.getOrDefault("did", "did:key:example-001");
        String securityLevel = payload.getOrDefault("securityLevel", "TRUSTED_ENVIRONMENT");

        ClientIdentityProfile profile = new ClientIdentityProfile();
        profile.setCid(cid);
        profile.setDid(did);
        profile.setSecurityLevel(securityLevel);
        profile.setPublicKey("simulated-public-key");

        return ResponseEntity.ok(Map.of(
                "message", "identity generated",
                "cid", profile.getCid(),
                "did", profile.getDid(),
                "securityLevel", profile.getSecurityLevel(),
                "publicKey", profile.getPublicKey()
        ));
    }

    @PostMapping("/attestation/generate")
    public ResponseEntity<Map<String, Object>> generateAttestation(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(Map.of(
                "message", "SIMULATED KEY ATTESTATION generated",
                "securityLevel", payload.getOrDefault("securityLevel", "TRUSTED_ENVIRONMENT"),
                "packageName", "com.bank.mobile",
                "verifiedBoot", "VERIFIED",
                "bootloaderLocked", true
        ));
    }

    @PostMapping("/enroll")
    public ResponseEntity<Map<String, Object>> enroll(@RequestBody Map<String, String> payload) {
        return ResponseEntity.status(201).body(Map.of(
                "message", "enrolled",
                "cid", payload.getOrDefault("cid", "client-001"),
                "did", payload.getOrDefault("did", "did:key:example-001"),
                "status", "ACTIVE"
        ));
    }

    @GetMapping("/challenge")
    public ResponseEntity<Map<String, Object>> challenge(@RequestParam String cid) {
        return ResponseEntity.ok(Map.of(
                "challengeId", "simulated-challenge",
                "cid", cid,
                "nonce", "simulated-nonce",
                "expiresAt", "2026-09-08T12:02:00Z"
        ));
    }

    @PostMapping("/verify-proof")
    public ResponseEntity<Map<String, Object>> verifyProof(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "cid", payload.getOrDefault("cid", "client-001"),
                "did", payload.getOrDefault("did", "did:key:example-001"),
                "clientStatus", "ACTIVE",
                "identityLevel", "DEVICE_KEY_PROVEN"
        ));
    }
}
