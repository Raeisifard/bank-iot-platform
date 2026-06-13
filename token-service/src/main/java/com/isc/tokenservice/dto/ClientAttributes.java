package com.isc.tokenservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientAttributes {

    @JsonProperty("iss")
    private String iss;

    @JsonProperty("aud")
    private String aud;

    @JsonProperty("jti")
    private String jti;

    @JsonProperty("sid")
    private String sid;

    @JsonProperty("did")
    private String did;

    @JsonProperty("cid")
    private String cid;
}