package com.isc.transaction.controller;

import com.isc.common.dto.ClientAttributes;
import com.isc.security.model.JwtClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

public class TransController {
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            Authentication authentication) {

        ClientAttributes claims =
                (ClientAttributes) authentication.getPrincipal();

        String clientId = claims.getCid();

        return ResponseEntity.ok(clientId);
    }
}
