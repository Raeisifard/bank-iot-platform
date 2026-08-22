package com.isc.common.security.authentication;

import com.isc.common.security.model.SecurityPrincipal;

public interface AuthenticationService {
    SecurityPrincipal authenticate(String bearerToken);
}
