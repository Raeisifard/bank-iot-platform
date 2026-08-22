package com.isc.token.keymanagement;

import java.util.List;

public interface JwtKeyPolicyService {
    JwtKeyPolicy getPolicy();

    JwtKeyVersion getActiveVersion();

    List<JwtKeyVersion> getPublishableVersions();
}
