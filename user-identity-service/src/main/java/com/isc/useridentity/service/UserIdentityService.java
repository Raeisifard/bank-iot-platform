package com.isc.useridentity.service;
import com.isc.useridentity.api.ApiDtos; import com.isc.useridentity.domain.*; import com.isc.useridentity.entity.UserIdentity; import com.isc.useridentity.exception.ApiException; import com.isc.useridentity.repository.UserIdentityRepository; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import java.util.Optional;
@Service @RequiredArgsConstructor public class UserIdentityService {
 private final UserIdentityRepository repo;
 public ApiDtos.UserResponse create(ApiDtos.CreateUserRequest r){ if(repo.existsById(r.userId())) throw new ApiException("USER_EXISTS","User already exists"); UserIdentity u=new UserIdentity();u.setUserId(r.userId());u.setCustomerId(r.customerId());u.setStatus(UserStatus.ACTIVE);u.setAssuranceLevel(AssuranceLevel.IAL_0);repo.save(u);return map(u); }
 public UserIdentity require(String id){return repo.findById(id).orElseThrow(()->new ApiException("USER_NOT_FOUND","User not found"));}
 public ApiDtos.UserResponse get(String id){return map(require(id));}
 public void ensureActive(UserIdentity u){if(u.getStatus()!=UserStatus.ACTIVE) throw new ApiException("USER_NOT_ACTIVE","User is not active");}
 private ApiDtos.UserResponse map(UserIdentity u){return new ApiDtos.UserResponse(u.getUserId(),u.getCustomerId(),u.getStatus(),u.getAssuranceLevel());}
}
