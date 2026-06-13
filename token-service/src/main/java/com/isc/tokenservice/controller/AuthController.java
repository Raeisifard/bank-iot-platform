package com.isc.tokenservice.controller;

import com.isc.tokenservice.dto.AuthTokens;
import com.isc.tokenservice.dto.LoginRequest;
import com.isc.tokenservice.dto.RefreshTokenRequest;
import com.isc.tokenservice.service.JwtGeneratorService;
import com.isc.tokenservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtGeneratorService jwtGeneratorService;

    @PostMapping("/token")
    public AuthTokens token(@RequestBody LoginRequest req) throws Exception {

        return jwtGeneratorService.generate(
                req.getCustomerId(),
                req.getDeviceId(),
                req.getClientId()
        );
    }

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public AuthTokens refresh(
            @RequestBody RefreshTokenRequest request)
            throws Exception {

        return refreshTokenService.refresh(
                request.getRefreshToken()
        );
    }
}