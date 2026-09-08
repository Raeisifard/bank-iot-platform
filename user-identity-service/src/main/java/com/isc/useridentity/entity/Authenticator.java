package com.isc.useridentity.entity;

import com.isc.useridentity.domain.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="UI_AUTHENTICATOR") @Getter @Setter @NoArgsConstructor
public class Authenticator {
    @Id @GeneratedValue(strategy=GenerationType.UUID) @Column(name="AUTHENTICATOR_ID", length=36) private String authenticatorId;
    @Column(name="USER_ID", nullable=false, length=64) private String userId;
    @Enumerated(EnumType.STRING) @Column(name="METHOD", nullable=false, length=32) private AuthenticationMethod method;
    @Column(name="REFERENCE", length=256) private String reference;
    @Enumerated(EnumType.STRING) @Column(name="STATUS", nullable=false, length=32) private AuthenticatorStatus status;
    @Column(name="CREATED_AT", nullable=false) private Instant createdAt;
    @PrePersist void prePersist(){ createdAt=Instant.now(); }
}
