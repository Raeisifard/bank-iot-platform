package com.isc.token.service;

import com.isc.token.dto.TokenRequest;
import com.isc.token.dto.TokenResponse;

public interface TokenService {
    TokenResponse issue(TokenRequest request);
}
