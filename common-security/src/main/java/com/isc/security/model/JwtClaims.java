package com.isc.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.isc.common.dto.ClientAttributes;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class JwtClaims {

    private String iss;
    private String aud;
    private Instant iat;
    private Instant exp;

    @JsonProperty("client_attrs")
    private ClientAttributes ca;
}