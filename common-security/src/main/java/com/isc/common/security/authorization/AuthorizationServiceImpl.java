package com.isc.common.security.authorization;

import java.util.Set;

public class AuthorizationServiceImpl implements AuthorizationService {
    @Override
    public boolean isAllowed(Set<String> authorities, String required) {
        return required != null && authorities != null && authorities.contains(required);
    }
}
