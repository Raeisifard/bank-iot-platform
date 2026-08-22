package com.isc.common.security.authorization;

import java.util.Set;

public interface AuthorizationService {
    boolean isAllowed(Set<String> authorities, String required);
}
