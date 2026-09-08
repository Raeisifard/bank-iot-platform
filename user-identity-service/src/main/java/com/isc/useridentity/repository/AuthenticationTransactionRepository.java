package com.isc.useridentity.repository;
import com.isc.useridentity.entity.AuthenticationTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuthenticationTransactionRepository extends JpaRepository<AuthenticationTransaction,String> {}
