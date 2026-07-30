package com.isc.transaction.controller;

import com.isc.security.model.JwtClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

public class TransController {
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            Authentication authentication) {

        JwtClaims claims =
                (JwtClaims) authentication.getPrincipal();

        String clientId =
                claims.getClientId();

        return ResponseEntity.ok(clientId);
    }
}
