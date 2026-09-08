package com.isc.clientidentity.controller;

import com.isc.clientidentity.api.ChallengeResponse;
import com.isc.clientidentity.api.RegisterClientRequest;
import com.isc.clientidentity.api.RegisterClientResponse;
import com.isc.clientidentity.api.VerifyProofRequest;
import com.isc.clientidentity.api.VerifyProofResponse;
import com.isc.clientidentity.service.ClientIdentityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/client-identities")
public class ClientIdentityController {
    private final ClientIdentityService service;

    public ClientIdentityController(ClientIdentityService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RegisterClientResponse> register(@Valid @RequestBody RegisterClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }

    @PostMapping("/{cid}/challenges")
    public ChallengeResponse challenge(@PathVariable String cid) {
        return service.issueChallenge(cid);
    }

    @PostMapping("/verify")
    public VerifyProofResponse verify(@Valid @RequestBody VerifyProofRequest request) {
        return service.verify(request);
    }

    @DeleteMapping("/{cid}")
    public ResponseEntity<Void> revoke(@PathVariable String cid) {
        service.revoke(cid);
        return ResponseEntity.noContent().build();
    }
}