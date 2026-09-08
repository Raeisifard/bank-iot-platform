package com.isc.useridentity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="UI_AUTH_AUDIT") @Getter @Setter @NoArgsConstructor
public class AuthenticationAudit {
    @Id @GeneratedValue(strategy=GenerationType.UUID) @Column(name="AUDIT_ID", length=36) private String auditId;
    @Column(name="AUTHENTICATION_ID", length=64) private String authenticationId;
    @Column(name="USER_ID", length=64) private String userId;
    @Column(name="CLIENT_ID", length=128) private String clientId;
    @Column(name="EVENT_TYPE", nullable=false, length=64) private String eventType;
    @Column(name="METHOD", length=32) private String method;
    @Column(name="DETAIL", length=1000) private String detail;
    @Column(name="CREATED_AT", nullable=false) private Instant createdAt;
    @PrePersist void prePersist(){ createdAt=Instant.now(); }
}
