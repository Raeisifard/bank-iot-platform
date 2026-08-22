package com.isc.token.jwks;

import java.util.List;
import java.util.Map;

public interface JwksService {
    List<Map<String, Object>> getJwks();
}
