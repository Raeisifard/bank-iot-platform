package com.isc.useridentity.controller;
import com.isc.useridentity.api.ApiDtos; import com.isc.useridentity.domain.AssuranceLevel; import com.isc.useridentity.service.AuthenticationService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/authentication") @RequiredArgsConstructor public class AuthenticationController { private final AuthenticationService s;
 @PostMapping("/transactions") public ApiDtos.AuthenticationContext start(@Valid @RequestBody ApiDtos.StartAuthenticationRequest r){return s.start(r);}
 @GetMapping("/transactions/{id}") public ApiDtos.AuthenticationContext get(@PathVariable String id){return s.get(id);}
 @PostMapping("/transactions/{id}/step-up") public ApiDtos.AuthenticationContext step(@PathVariable String id,@Valid @RequestBody ApiDtos.StepUpRequest r){return s.stepUp(id,r);}
 @PostMapping("/transactions/{id}/otp/challenge") public ApiDtos.OtpChallengeResponse otp(@PathVariable String id){return s.issueOtp(id);}
 @PostMapping("/transactions/{id}/otp/verify") public ApiDtos.AuthenticationContext verifyOtp(@PathVariable String id,@Valid @RequestBody ApiDtos.OtpVerifyRequest r){return s.verifyOtp(id,r);}
 @PostMapping("/transactions/{id}/biometric/verify") public ApiDtos.AuthenticationContext biometric(@PathVariable String id,@Valid @RequestBody ApiDtos.BiometricVerifyRequest r){return s.verifyBiometric(id,r);}
 @PostMapping("/transactions/{id}/revoke") public void revoke(@PathVariable String id){s.revoke(id);}
}
