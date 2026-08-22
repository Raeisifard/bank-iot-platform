package com.isc.token.controller;

import com.isc.token.dto.*;
import com.isc.token.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final TokenService tokenService;

    public AuthController(TokenService s) {
        tokenService = s;
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> token(@Valid @RequestBody TokenRequest r) {
        return ResponseEntity.ok(tokenService.issue(r));
    }
}
