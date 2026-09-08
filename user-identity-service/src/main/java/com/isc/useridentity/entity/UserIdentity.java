package com.isc.useridentity.entity;

import com.isc.useridentity.domain.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="UI_USER_IDENTITY") @Getter @Setter @NoArgsConstructor
public class UserIdentity {
    @Id @Column(name="USER_ID", length=64) private String userId;
    @Column(name="CUSTOMER_ID", nullable=false, length=64) private String customerId;
    @Enumerated(EnumType.STRING) @Column(name="STATUS", nullable=false, length=32) private UserStatus status;
    @Enumerated(EnumType.STRING) @Column(name="ASSURANCE_LEVEL", nullable=false, length=16) private com.isc.useridentity.domain.AssuranceLevel assuranceLevel;
    @Column(name="CREATED_AT", nullable=false) private Instant createdAt;
    @Column(name="UPDATED_AT", nullable=false) private Instant updatedAt;
    @PrePersist void prePersist(){ createdAt=Instant.now(); updatedAt=createdAt; }
    @PreUpdate void preUpdate(){ updatedAt=Instant.now(); }
}
