package com.isc.useridentity.entity;

import com.isc.useridentity.domain.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="UI_AUTH_TRANSACTION") @Getter @Setter @NoArgsConstructor
public class AuthenticationTransaction {
    @Id @Column(name="AUTHENTICATION_ID", length=64) private String authenticationId;
    @Column(name="USER_ID", nullable=false, length=64) private String userId;
    @Column(name="CLIENT_ID", nullable=false, length=128) private String clientId;
    @Enumerated(EnumType.STRING) @Column(name="PURPOSE", nullable=false, length=40) private AuthenticationPurpose purpose;
    @Enumerated(EnumType.STRING) @Column(name="REQUIRED_LEVEL", nullable=false, length=16) private AssuranceLevel requiredAssuranceLevel;
    @Enumerated(EnumType.STRING) @Column(name="ACHIEVED_LEVEL", nullable=false, length=16) private AssuranceLevel achievedAssuranceLevel;
    @Enumerated(EnumType.STRING) @Column(name="METHOD", length=32) private AuthenticationMethod authenticationMethod;
    @Enumerated(EnumType.STRING) @Column(name="STATUS", nullable=false, length=32) private TransactionStatus status;
    @Column(name="AUTHENTICATED_AT") private Instant authenticatedAt;
    @Column(name="EXPIRES_AT", nullable=false) private Instant expiresAt;
    @Column(name="CREATED_AT", nullable=false) private Instant createdAt;
}
