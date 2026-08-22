package com.isc.token.controller;

import com.isc.token.dto.JwksResponse;
import com.isc.token.jwks.JwksService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/.well-known")
public class JwksController {
    private final JwksService service;

    public JwksController(JwksService s) {
        service = s;
    }

    @GetMapping("/jwks.json")
    public ResponseEntity<JwksResponse> jwks() {
        return ResponseEntity.ok(new JwksResponse(service.getJwks()));
    }
}
