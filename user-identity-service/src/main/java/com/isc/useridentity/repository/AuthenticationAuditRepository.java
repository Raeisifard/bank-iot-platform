package com.isc.useridentity.repository;
import com.isc.useridentity.entity.AuthenticationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuthenticationAuditRepository extends JpaRepository<AuthenticationAudit,String> {}
