package com.isc.useridentity.repository;
import com.isc.useridentity.entity.Authenticator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AuthenticatorRepository extends JpaRepository<Authenticator,String> { List<Authenticator> findByUserId(String userId); }
