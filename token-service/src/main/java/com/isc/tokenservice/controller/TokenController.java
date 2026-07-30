package com.isc.tokenservice.controller;

import com.isc.tokenservice.dto.AuthTokens;
import com.isc.tokenservice.dto.LoginRequest;
import com.isc.tokenservice.dto.RefreshTokenRequest;
import com.isc.tokenservice.identity.RefreshTokenService;
import com.isc.tokenservice.service.AuthenticationService;
import com.isc.tokenservice.service.JwtGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class TokenController {

    private final AuthenticationService authenticationService;
    private final JwtGeneratorService jwtGeneratorService;

    @PostMapping("/token")
    public AuthTokens token(@RequestBody LoginRequest req) throws Exception {

        return authenticationService.login(req);
    }

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public AuthTokens refresh(
            @RequestBody RefreshTokenRequest req)
            throws Exception {

        return authenticationService.refresh(req);
    }
}