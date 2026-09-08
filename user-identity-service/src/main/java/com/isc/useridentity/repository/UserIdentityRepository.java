package com.isc.useridentity.repository;
import com.isc.useridentity.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserIdentityRepository extends JpaRepository<UserIdentity,String> {}
