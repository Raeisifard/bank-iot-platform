package com.isc.token.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn, String refreshToken) {
}
